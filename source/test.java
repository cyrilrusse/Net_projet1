public class test {
    public static void main(String[] argv){
        int t1 = 3;
        int t2 = 4;

        byte[] array1 = new byte[t1];
        for(int i=0; i<t1; i++)
            array1[i] = (byte)(i+1);

        byte[]  array2 = new byte[t2];
        for(int i=0; i<t2; i++)
            array2[i] = (byte) (t1 + i+1);

        byte[] array3 = new byte[t1+t2+2];
        array3[0] = 0;
        array3[t1+1] = 0;
        System.arraycopy(array1, 0, array3, 1, t1);
        System.arraycopy(array2, 0, array3, t1+2, t2);
        for(int i=0; i<t1+t2+2; i++)
            System.out.println(array3[i]);
    }
}
