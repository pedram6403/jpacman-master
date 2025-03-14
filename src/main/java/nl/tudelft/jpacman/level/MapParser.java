package nl.tudelft.jpacman.level;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nl.tudelft.jpacman.PacmanConfigurationException;
import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.BoardFactory;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.npc.Ghost;

/**
 * Creates new {@link Level}s from text representations.
 *
 * @author Jeroen Roosen
 */
public class MapParser {

    /**
     * The factory that creates the levels.
     */
    private final LevelFactory levelCreator;

    /**
     * The factory that creates the squares and board.
     */
    private final BoardFactory boardCreator;

    /**
     * Creates a new map parser.
     *
     * @param levelFactory
     *            The factory providing the NPC objects and the level.
     * @param boardFactory
     *            The factory providing the Square objects and the board.
     */
    public MapParser(LevelFactory levelFactory, BoardFactory boardFactory) {
        this.levelCreator = levelFactory;
        this.boardCreator = boardFactory;
    }

    /**
     * Parses the text representation of the board into an actual level.
     *
     * <ul>
     * <li>Supported characters:
     * <li>' ' (space) an empty square.
     * <li>'#' (bracket) a wall.
     * <li>'.' (period) a square with a pellet.
     * <li>'P' (capital P) a starting square for players.
     * <li>'G' (capital G) a square with a ghost.
     * </ul>
     *
     * @param map
     *            The text representation of the board, with map[x][y]
     *            representing the square at position x,y.
     * @return The level as represented by this text.
     */
    // public Level parseMap(char[][] map) {
    //     int width = map.length;
    //     int height = map[0].length;

    //     Square[][] grid = new Square[width][height];

    //     List<Ghost> ghosts = new ArrayList<>();
    //     List<Square> startPositions = new ArrayList<>();

    //     makeGrid(map, width, height, grid, ghosts, startPositions);
        

    //     Board board = boardCreator.createBoard(grid);
    //     return levelCreator.createLevel(board, ghosts, startPositions);
    // }
    public Level parseMap(char[][] map) {
        MapData mapData = new MapData(map); // Crée une instance de MapData
        makeGrid(mapData); // Génère le plateau et les entités
    
        Board board = boardCreator.createBoard(mapData.grid);
        return levelCreator.createLevel(board, mapData.ghosts, mapData.startPositions);
    }

/**
 * Structure contenant les informations du plateau.
 */
public static class MapData {
    final char[][] map;
    final Square[][] grid;
    final List<Ghost> ghosts;
    final List<Square> startPositions;

    public MapData(char[][] map) {
        this.map = map;
        this.grid = new Square[map.length][map[0].length];
        this.ghosts = new ArrayList<>();
        this.startPositions = new ArrayList<>();
    }
}

private void makeGrid(MapData mapData) {
    for (int x = 0; x < mapData.map.length; x++) {
        for (int y = 0; y < mapData.map[0].length; y++) {
            char c = mapData.map[x][y];
            SquareInfo squareInfo = new SquareInfo(x, y, c);
            addSquare(mapData.grid, mapData.ghosts, mapData.startPositions, squareInfo);
        }
    }
}


//     private void makeGrid(char[][] map, int width, int height,
//                       Square[][] grid, List<Ghost> ghosts, List<Square> startPositions) {
//     for (int x = 0; x < width; x++) {
//         for (int y = 0; y < height; y++) {
//             char c = map[x][y];
//             SquareInfo squareInfo = new SquareInfo(x, y, c);
//             addSquare(grid, ghosts, startPositions, squareInfo);
//         }
//     }
// }

    /**
     * Classe contenant les informations d'une case sur la carte.
     */
    public static class SquareInfo {
        final int x;
        final int y;
        final char type;
    
        public SquareInfo(int x, int y, char type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }
    }
    private Square createGroundSquare() {
        return boardCreator.createGround();
    }
    
    private Square createWallSquare() {
        return boardCreator.createWall();
    }
    
    private Square createPelletSquare() {
        Square pelletSquare = boardCreator.createGround();
        levelCreator.createPellet().occupy(pelletSquare);
        return pelletSquare;
    }
    
    private Square createGhostSquare(List<Ghost> ghosts) {
        Ghost ghost = levelCreator.createGhost();
        return makeGhostSquare(ghosts, ghost);
    }
    
    private Square createPlayerSquare(List<Square> startPositions) {
        Square playerSquare = boardCreator.createGround();
        startPositions.add(playerSquare);
        return playerSquare;
    }
    
    /**
 * Ajoute une case au plateau en fonction du caractère du fichier de carte.
 *
 * @param grid            La grille du plateau.
 * @param ghosts          Liste des fantômes présents sur la carte.
 * @param startPositions  Liste des positions de départ des joueurs.
 * @param squareInfo      Informations sur la case actuelle.
 */
protected void addSquare(Square[][] grid, List<Ghost> ghosts,
                         List<Square> startPositions, SquareInfo squareInfo) {
    switch (squareInfo.type) {
        case ' ':
            grid[squareInfo.x][squareInfo.y] = createGroundSquare();
            break;
        case '#':
            grid[squareInfo.x][squareInfo.y] = createWallSquare();
            break;
        case '.':
            grid[squareInfo.x][squareInfo.y] = createPelletSquare();
            break;
        case 'G':
            grid[squareInfo.x][squareInfo.y] = createGhostSquare(ghosts);
            break;
        case 'P':
            grid[squareInfo.x][squareInfo.y] = createPlayerSquare(startPositions);
            break;
        default:
            throw new PacmanConfigurationException("Caractère invalide à la position "
                + squareInfo.x + "," + squareInfo.y + ": " + squareInfo.type);
    }
}


    // /**
    //  * Adds a square to the grid based on a given character. These
    //  * character come from the map files and describe the type
    //  * of square.
    //  *
    //  * @param grid
    //  *            The grid of squares with board[x][y] being the
    //  *            square at column x, row y.
    //  * @param ghosts
    //  *            List of all ghosts that were added to the map.
    //  * @param startPositions
    //  *            List of all start positions that were added
    //  *            to the map.
    //  * @param x
    //  *            x coordinate of the square.
    //  * @param y
    //  *            y coordinate of the square.
    //  * @param c
    //  *            Character describing the square type.
    //  */
    // protected void addSquare(Square[][] grid, List<Ghost> ghosts,
    //                          List<Square> startPositions, int x, int y, char c) {
    //     switch (c) {
    //         case ' ':
    //             grid[x][y] = boardCreator.createGround();
    //             break;
    //         case '#':
    //             grid[x][y] = boardCreator.createWall();
    //             break;
    //         case '.':
    //             Square pelletSquare = boardCreator.createGround();
    //             grid[x][y] = pelletSquare;
    //             levelCreator.createPellet().occupy(pelletSquare);
    //             break;
    //         case 'G':
    //             Square ghostSquare = makeGhostSquare(ghosts, levelCreator.createGhost());
    //             grid[x][y] = ghostSquare;
    //             break;
    //         case 'P':
    //             Square playerSquare = boardCreator.createGround();
    //             grid[x][y] = playerSquare;
    //             startPositions.add(playerSquare);
    //             break;
    //         default:
    //             throw new PacmanConfigurationException("Invalid character at "
    //                 + x + "," + y + ": " + c);
    //     }
    // }

    /**
     * creates a Square with the specified ghost on it
     * and appends the placed ghost into the ghost list.
     *
     * @param ghosts all the ghosts in the level so far, the new ghost will be appended
     * @param ghost the newly created ghost to be placed
     * @return a square with the ghost on it.
     */
    protected Square makeGhostSquare(List<Ghost> ghosts, Ghost ghost) {
        Square ghostSquare = boardCreator.createGround();
        ghosts.add(ghost);
        ghost.occupy(ghostSquare);
        return ghostSquare;
    }

    /**
     * Parses the list of strings into a 2-dimensional character array and
     * passes it on to {@link #parseMap(char[][])}.
     *
     * @param text
     *            The plain text, with every entry in the list being a equally
     *            sized row of squares on the board and the first element being
     *            the top row.
     * @return The level as represented by the text.
     * @throws PacmanConfigurationException If text lines are not properly formatted.
     */
    public Level parseMap(List<String> text) {

        checkMapFormat(text);

        int height = text.size();
        int width = text.get(0).length();

        char[][] map = new char[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = text.get(y).charAt(x);
            }
        }
        return parseMap(map);
    }

    
    /**
 * Vérifie la validité du format de la carte.
 * @param text Liste des lignes de la carte.
 * @throws PacmanConfigurationException si le format est invalide.
 */
private void checkMapFormat(List<String> text) {
    validateNotNull(text);
    validateNotEmpty(text);
    int width = validateLineWidth(text);
    validateConsistentWidth(text, width);
}

/**
 * Vérifie que la carte n'est pas null.
 */
private void validateNotNull(List<String> text) {
    if (text == null) {
        throw new PacmanConfigurationException("Input text cannot be null.");
    }
}

/**
 * Vérifie que la carte contient au moins une ligne.
 */
private void validateNotEmpty(List<String> text) {
    if (text.isEmpty()) {
        throw new PacmanConfigurationException("Input text must consist of at least 1 row.");
    }
}

/**
 * Vérifie que la première ligne n'est pas vide et retourne sa largeur.
 */
private int validateLineWidth(List<String> text) {
    int width = text.get(0).length();
    if (width == 0) {
        throw new PacmanConfigurationException("Input text lines cannot be empty.");
    }
    return width;
}

/**
 * Vérifie que toutes les lignes ont la même largeur.
 */
private void validateConsistentWidth(List<String> text, int width) {
    for (String line : text) {
        if (line.length() != width) {
            throw new PacmanConfigurationException("Input text lines are not of equal width.");
        }
    }
}


    // /**
    //  * Check the correctness of the map lines in the text.
    //  * @param text Map to be checked
    //  * @throws PacmanConfigurationException if map is not OK.
    //  */
    // private void checkMapFormat(List<String> text) {
    //     if (text == null) {
    //         throw new PacmanConfigurationException(
    //             "Input text cannot be null.");
    //     }

    //     if (text.isEmpty()) {
    //         throw new PacmanConfigurationException(
    //             "Input text must consist of at least 1 row.");
    //     }

    //     int width = text.get(0).length();

    //     if (width == 0) {
    //         throw new PacmanConfigurationException(
    //             "Input text lines cannot be empty.");
    //     }

    //     for (String line : text) {
    //         if (line.length() != width) {
    //             throw new PacmanConfigurationException(
    //                 "Input text lines are not of equal width.");
    //         }
    //     }
    // }

    /**
     * Parses the provided input stream as a character stream and passes it
     * result to {@link #parseMap(List)}.
     *
     * @param source
     *            The input stream that will be read.
     * @return The parsed level as represented by the text on the input stream.
     * @throws IOException
     *             when the source could not be read.
     */
    public Level parseMap(InputStream source) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
            source, "UTF-8"))) {
            List<String> lines = new ArrayList<>();
            while (reader.ready()) {
                lines.add(reader.readLine());
            }
            return parseMap(lines);
        }
    }

    /**
     * Parses the provided input stream as a character stream and passes it
     * result to {@link #parseMap(List)}.
     *
     * @param mapName
     *            Name of a resource that will be read.
     * @return The parsed level as represented by the text on the input stream.
     * @throws IOException
     *             when the resource could not be read.
     */
    @SuppressFBWarnings(
        value = {"OBL_UNSATISFIED_OBLIGATION", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"},
        justification = "try with resources always cleans up / false positive in java 11"
    )
    public Level parseMap(String mapName) throws IOException {
        try (InputStream boardStream = MapParser.class.getResourceAsStream(mapName)) {
            if (boardStream == null) {
                throw new PacmanConfigurationException("Could not get resource for: " + mapName);
            }
            return parseMap(boardStream);
        }
    }

    /**
     * @return the BoardCreator
     */
    protected BoardFactory getBoardCreator() {
        return boardCreator;
    }
}
