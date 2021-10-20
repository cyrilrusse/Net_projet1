public class Grid {
    static int[] possible_monster_position = new int[4];

    public void reset(){
        possible_monster_position[0] = 1;
        possible_monster_position[1] = 10;
        possible_monster_position[2] = 1;
        possible_monster_position[3] = 10;
    }

    public void intersectionString(String sensor_position){
        String[] position_split = sensor_position.split(":");
        // System.out.println(position_split[0]);
        // System.out.println(position_split[1]);


        if(position_split[0].length()>3)
            return;
        int letter_to_digit = position_split[0].charAt(0);
        int sensor_position_digit = position_split[0].charAt(1);
        int range = position_split[1].charAt(0);



        if(letter_to_digit<65 || letter_to_digit>74){
            System.out.println("error 1");
            return;
        }
        letter_to_digit -=64;
        range -= 48;

        if(range<1 || range>3) {
            System.out.println("error 2");
            return;
        }
        sensor_position_digit -= 48;
        if(position_split[0].length()==3){
            if(position_split[0].charAt(1)==49 && position_split[0].charAt(2)==48)
                sensor_position_digit = 10;
            else{
                System.out.println("wrong format.");
                return;
            }
        }

        if(sensor_position_digit<1 || sensor_position_digit>10) {
            System.out.println("error 3");
            return;
        }

        int[] square_sensor = new int[4];
        square_sensor[0] = letter_to_digit - range;
        if(square_sensor[0]<1)
            square_sensor[0] = 1;
        square_sensor[1] = letter_to_digit + range;
        if(square_sensor[1]>10)
            square_sensor[1] = 10;
        square_sensor[2] = sensor_position_digit - range;
        if (square_sensor[2] < 1)
            square_sensor[2] = 1;
        square_sensor[3] = sensor_position_digit + range;
        if (square_sensor[3] > 10)
            square_sensor[3] = 10;

        if(square_sensor[0]>possible_monster_position[0])
            possible_monster_position[0] = square_sensor[0];
        if(square_sensor[1] <possible_monster_position[1])
            possible_monster_position[1] = square_sensor[1];
        if(square_sensor[2]>possible_monster_position[2])
            possible_monster_position[2] = square_sensor[2];
        if(square_sensor[3] < possible_monster_position[3])
            possible_monster_position[3] = square_sensor[3];

    }

    public void intersection(byte[] sensor_position, byte range) {

        if (sensor_position[0] < 65 || sensor_position[0] > 74) {
            System.out.println("1");
            return;
        }
        int int_range = range;
        int_range -= 48;

        if (int_range < 1 || int_range > 3) {
            System.out.println("2");
        }
        int letter_to_digit = sensor_position[0] - 64;
        int sensor_position_digit = sensor_position[1] - 48;
        if (sensor_position_digit < 1 || sensor_position_digit > 10) {
            System.out.println("3");
            return;
        }

        System.out.println("coucou");
        int[] square_sensor = new int[4];
        square_sensor[0] = letter_to_digit - int_range;
        if (square_sensor[0] < 1)
            square_sensor[0] = 1;
        square_sensor[1] = letter_to_digit + int_range;
        if (square_sensor[1] > 10)
            square_sensor[1] = 10;
        square_sensor[2] = sensor_position_digit - int_range;
        if (square_sensor[2] < 1)
            square_sensor[2] = 1;
        square_sensor[3] = sensor_position_digit + int_range;
        if (square_sensor[3] > 10)
            square_sensor[3] = 10;

        if (square_sensor[0] > possible_monster_position[0])
            possible_monster_position[0] = square_sensor[0];
        if (square_sensor[1] < possible_monster_position[1])
            possible_monster_position[1] = square_sensor[1];
        if (square_sensor[2] > possible_monster_position[2])
            possible_monster_position[2] = square_sensor[2];
        if (square_sensor[3] < possible_monster_position[3])
            possible_monster_position[3] = square_sensor[3];

    }


    public void display(){
        System.out.print("   ");
        for(int i = 1; i<11; i++)
            System.out.print(i+"  ");
        System.out.print("\n");
        char letter = 'A';

        for(int i=1; i<11; i++){
            System.out.print("   ");
            for(int j = 1; j<11; j++)
                System.out.print("-- ");
            System.out.print("\n");
            
            System.out.print(letter+" ");
            letter++;
            System.out.print("|");
            for(int j = 1; j<11; j++){
                if(i>=possible_monster_position[0]&&i<=possible_monster_position[1]&&j>=possible_monster_position[2]&&j<=possible_monster_position[3])
                    System.out.print(" X");
                else
                    System.out.print("  ");
                System.out.print("|");
            }
            System.out.print("\n");
        }
        System.out.print("   ");
        for (int j = 0; j < 10; j++)
            System.out.print("-- ");
        System.out.print("\n");
    }



    


    

    
}
