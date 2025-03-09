#!/usr/bin/perl -w
#
# duplocForCPP.pl - detect duplicated lines of code (algorithm only)
#
# Takes source code (or other) files and collects all linenumbers of
# lines equal to each other within these files. The algorithm is linear
# (in space and time) in the number of input lines.
# Removes C++ comments (Attention: gets confused if comment signs are
# found in literal strings!)
#
# Synopsis: duplocForCPP.pl filename [filename, ...]
#
# Filtering options have to be set in the code below.
#
# Output: Lists of CloneClasses,
#         each detailling the copied fragment and the locations
#         where the fragment is found.
#
###########################################################################
# Author:  Matthias Rieger
# History: - Feb 25, 2006
#            added minimal length in characters, plus some report niceties
###########################################################################
# $Id: simpleDude.pl,v 1.1.1.1 2006/11/15 13:00:28 rieger Exp $
###########################################################################

# Options to be set by user
$slidingWindowSize           = 10;  # number of lines per comparison
$removeKeywords              = 0;  # if 1, keywords from list below are removed
$equivalenceClassMinimalSize = 2;  # how many copies until we report the clone?
$fragmentMinimalCharSize     = 50; # fragment size which we report

# strings to be removed from the code, pre-comparison
@keywords = qw(if then else for { } );

# lines which will be skipped
@unwantedLines = qw(else return return; return result; }else{ { } ; );
@unwantedLines = qw(); # empty list cancels line skipping

#######################################################################
# Nothing to be changed below this line

$keywordsRegEx = join '|', @keywords;
push @unwantedLines, @keywords;
@unwantedLines{@unwantedLines} = (1) x @unwantedLines;

$totalLines = $emptyLines = $codeLines     = 0;
@currentLines = @currentLineNos = %eqLines = ();
$inComment                                 = 0;
#$startTime                                 = (times)[0];

# go over all input files
while(<>) {
  chomp;
  $totalLines++;

  # remove comments of type /* */
  my $codeOnly = '';
  while(($inComment && m|\*/|) || (!$inComment && m|/\*|)) {
    unless($inComment) { $codeOnly .= $` }
    $inComment = !$inComment;
    $_ = $';
  }
  $codeOnly .= $_ unless($inComment);
  $_ = $codeOnly;

	
  s|//.*$||;                                # remove comments of type //
  s/\s+//g;                                 # remove white space
  s/$keywordsRegEx//og if($removeKeywords); # remove keywords

  # skip empty and unwanted lines
  next if((! $_                       && $emptyLines++) ||
	  (defined $unwantedLines{$_} && $codeLines++)     );

  $codeLines++;
  push @currentLines, $_;
  push @currentLineNos, $.;
  if($slidingWindowSize < @currentLines) {
    shift @currentLines;
    shift @currentLineNos;
  }

  my $lineToBeCompared = join '', @currentLines;
  my $lineNumbersCompared = "<$ARGV>";  # prepend filename
  #  zero padding for linenumbers to enable lexical sorting later on
  $lineNumbersCompared .= join '/', map {sprintf "%07d",$_} @currentLineNos;

  if($bucketRef = $eqLines{$lineToBeCompared}) {
    push @$bucketRef, $lineNumbersCompared;
  } else {
    $eqLines{$lineToBeCompared} = [ $lineNumbersCompared ];
  }

  if(eof) {
    close ARGV;      # Reset linenumber count for next file
    $inComment = 0;  # Reset indicator for /* */ comments (just to make sure)
  }
}


#$processingTime = (times)[0] - $startTime;

# print the equivalence classes
$numOfMarkedEquivClasses = 0;
$numOfMarkedFragments     = 0;
foreach my $samelines (sort {length $a <=> length $b} keys %eqLines) {
  my @locations = @{$eqLines{$samelines}};
  if(scalar @locations  >= $equivalenceClassMinimalSize  &&
    length $samelines   >= $fragmentMinimalCharSize         )  {
    $numOfMarkedEquivClasses++;
    $numOfMarkedFragments += scalar @locations;
    print "------\nCloneClass \#$numOfMarkedEquivClasses: @{[scalar @locations]} Members\n";
    my $pos=0;
    # format concatenated lines in a somewhat nice way
    foreach (map {$_.=';' } split /;/, $samelines) {
      print $pos++==0?" Code: $_\n":"       $_\n";
    }
    $pos=0;
    foreach (sort @locations) {
      s!(>|/)0+!$1!g; # remove zero-padding after we're done sorting
      print $pos++==0?" Loc.: $_\n":"       $_\n";
    }
  }
}

print "\n\n\n";
printf "Processed:     %7d lines\n",$totalLines;
printf "Code:          %7d lines\n",$codeLines;
printf "Empty/Comment: %7d lines\n",$emptyLines;
#printf "Scanning time: %7.2f sec (%.f lines/sec)\n",$processingTime,$codeLines/$processingTime;
print  "--------------------------------------------\n";
printf "Sliding window size:           %5d lines\n",$slidingWindowSize;
printf "Equiv-class lower bound:       %5d members\n",$equivalenceClassMinimalSize;
printf "Fragment size lower bound:     %5d chars\n",$fragmentMinimalCharSize;
printf "Total equivalence classes:     %5d\n",scalar keys %eqLines;
printf "Reported equivalence classes:  %5d\n",$numOfMarkedEquivClasses;
printf "Reported Fragments:            %5d\n",$numOfMarkedFragments;


