public class Grid {
    static int[] possible_monster_position = new int[4];

    public void reset(){
        possible_monster_position[0] = 1;
        possible_monster_position[1] = 10;
        possible_monster_position[2] = 1;
        possible_monster_position[3] = 10;
    }

    public void intersection(byte[] sensor_position, byte range){

        
        if(sensor_position[0]<65 | sensor_position[0]>74)
            return;
        if(range<1 || range>3)
            return;
        int letter_to_digit = sensor_position[0]-64;
        int sensor_position_digit = sensor_position[1]-48;
        if(sensor_position_digit<1 || sensor_position_digit>10)
            return;


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
