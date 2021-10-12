package test;
import javafx.util.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

enum Direction8 {
    NORTH(0, -1), NORTH_EAST (1,-1), EAST(1, 0), SOUTH_EAST(1,1), SOUTH(0, 1),
    SOUTH_WEST(-1,1), WEST(-1, 0), NORTH_WEST(-1,-1);

    private final int dx;
    private final int dy;

    Direction8(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }
    public int getX() { return dx; }
    public int getY() { return dy; }
}

public class MHGame {

    boolean cheat;
    //List<List<String> > iterations_cheat;
    Random r;
    int[] sensors_range = new int[]{1,1,1,2,2,3}; // (1: 1/2, 2: 2/6, 3: 1/6)

    // Monster position
    int x;
    int y;
    int[][] board;
    int nb_measures = 0; // Nb measures received (for Subscriber use)

    public MHGame (boolean _cheat) {
        this.board = new int[10][10];
        this.reset();
        this.cheat = _cheat;

        //if (cheat) {
        //    this.iterations_cheat = new ArrayList<>();
        //    this.iterations_cheat.add(Arrays.asList("E5", "F5:2","D4:1"));
        //    this.iterations_cheat.add(Arrays.asList("E6", "E6:2","D6:1"));
        //    this.iterations_cheat.add(Arrays.asList("D7", "E6:2","A10:3"));
        //    this.iterations_cheat.add(Arrays.asList("C7", "D9:2","B6:1","B7:1"));
        //    this.iterations_cheat.add(Arrays.asList("C8", "E8:2","B9:3"));
        //    this.iterations_cheat.add(Arrays.asList("D9", "C8:1","D9:1"));
        //}
    }

    /**
     * Chooses first monster position
     */
    public void launch_game () {
        r = new Random();
        x = r.nextInt(10);
        y = r.nextInt(10);
    }

    /**
     * Makes the monster advance one tile.
     * @return a list of sensors and their measurement for this iteration.
     */
    public List<String> next_iteration () {

        /* Monster advances */
        reset();
        move_monster();

        /* Sensors */
        int nb_sensors = r.nextInt(2) + 2; // 2 or 3 sensors will fire.
        ArrayList<String> measures = new ArrayList<>();
        while (nb_measures != nb_sensors) {
            int range_sensor = sensors_range[r.nextInt(sensors_range.length)]; // Pick the range of the sensor
            //Pick its position on the grid
            int sensor_x = pick_coordinate(range_sensor,x);
            int sensor_y = pick_coordinate(range_sensor,y);

            // Mark board with sensor
            add (range_sensor, sensor_x, sensor_y, 1);
            nb_measures++;
            if (!sensor_valid ()) {
                add(range_sensor, sensor_x, sensor_y, -1); // Remove bad sensor
                nb_measures--;
                continue;
            }
            // Record sensor if valid
            measures.add(write_position (sensor_x, sensor_y,range_sensor));
        }
        if (cheat) {
            System.out.println("--- Cheating mode ---");
            System.out.println("\tMonster position: "+ write_position(x,y));
            for (String m: measures) {
                System.out.println("\t"+m);
            }
            System.out.println("----------------");
        }
        return measures;
    }

    /**
     * Count the number of tiles whose value is equal to nb_measures.
     * @return true if that number >=2. False otherwise
     */
    private boolean sensor_valid () {
        int nb = 0;
        for (int i = 0; i<10; i++) {
            for (int j = 0; j<10; j++) {
                if (board[i][j] == nb_measures)
                    nb++;
            }
        }
        return nb >=2;
    }

    private int pick_coordinate (int range_sensor, int position) {
        int start_x = Math.max(position - range_sensor, 0); //ex: 4 - 1 = 3
        int end_x = Math.min(position + range_sensor, 9) + 1; //ex: 4 + 1 + 1 = 6
        int range = end_x - start_x; // 3
        return r.nextInt(range) + start_x; // [0, 1, or 2] + 3 = [3, 4, or 5]
    }

    void move_monster() {
        int new_x;
        int new_y;
        Direction8 direction;
        do {
            direction = Direction8.values()[r.nextInt(Direction8.values().length)];
            new_x = x + direction.getX();
            new_y = y + direction.getY();
        } while ((new_y < 0 || new_y > 9) || (new_x < 0 || new_x > 9));
        move (direction);
    }

    void move(Direction8 direction) {
        this.x += direction.getX();
        this.y += direction.getY();
    }

    /**
     * @param data the data submitted by the user (format: B3)
     * @return the corresponding x,y coordinates
     */
    private Pair<Integer,Integer> extract_position (String data) throws IOException {
        /* Check data consistency */
        String[] position = data.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        if (position.length != 2)
            throw new IOException("Unexpected Application Message: " + data);
        int x = position[0].charAt(0) - 'A';
        int y = Integer.parseInt(position[1])-1; // Tab indexing starts at 1 for player
        if ((y < 0 || y > 9) || (x < 0 || x > 9))
            throw new IOException("Unexpected Application Message: " + data);
        return new Pair<>(x,y);
    }

    private String write_position (int x, int y) {
        char x_s = (char) (x + 'A');
        return x_s + Integer.toString(y+1);
    }

    private String write_position (int x, int y, int precision) {
        char x_s = (char) (x + 'A');
        return x_s + Integer.toString(y+1) + ":" + precision;
    }

    /**
     * Determines if the guess made by the user corresponds to the monster's position.
     * @param data the data submitted by the user (format: B3)
     * @return true is the guess was right, false otherwise.
     */
    public boolean submit_guess (String data) throws IOException {
        /* Check data consistency */
        Pair<Integer,Integer> position = extract_position(data);

        /* Play the game */
        return (position.getKey() == x) && (position.getValue() == y);
    }

    /**
     * Removes all previous measurements
     */
    public void reset () {
        for (int[] ints : board) {
            Arrays.fill(ints, 0);
        }
        nb_measures = 0;
    }

    /**
     *
     * @param data Application message (format B3:2)
     */
    public void add_measure (String data) throws IOException, NumberFormatException {
        /* Parse data*/
        String[] split = data.split(":");
        if (split.length != 2)
            throw new IOException("Unexpected Application Message: "+ data);
        /* Precision */
        int precision = Integer.parseInt(split[1]);
        if (precision < 1 || precision > 3)
            throw new IOException("Unexpected Application Message: "+ data);
        /* Grid position */
        Pair<Integer,Integer> position = extract_position(split[0]);
        int x = position.getKey();
        int y = position.getValue();

        /* Add measure */
        add (precision,x,y,1);
        nb_measures++;
    }

    private void add (int precision, int x, int y, int sub) {
        for (int i = Math.max(x - precision, 0); i <= x+precision && i <= 9; i++) {
            for (int j = Math.max(y - precision, 0); j <= y+precision && j <= 9; j++) {
                board[i][j] += sub;
            }
        }
    }

    public void display () {
        System.out.println("The monster is situated somewhere on the tiles marked by a cross.");
        System.out.println("    1  2  3  4  5  6  7  8  9 10");
        for (int i = 0; i<=9; i++) {
            System.out.println("   -- -- -- -- -- -- -- -- -- --");
            char l = (char) (i+'A');
            System.out.print(l + " ");
            for (int j = 0; j <= 9; j++) {
                String to_print = (board[i][j] == nb_measures)? "| X": "|  ";
                System.out.print(to_print);
            }
            System.out.println("|");
        }
        System.out.println("   -- -- -- -- -- -- -- -- -- --");
    }
}
