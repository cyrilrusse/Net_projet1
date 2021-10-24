public class Message{
    int type;
    String[] words;
    int nbr_of_words;

    public Message(int type, String[] words, int nbr_of_words){
        this.type = type;
        this.nbr_of_words = nbr_of_words;
        this.words = words;
    }

    public int getType(){
        return type;
    }

    public String getWordAt(int number_word){
        if(number_word>nbr_of_words)
            return null;
        return words[number_word];
    }

    public int getNbrOfWords(){
        return nbr_of_words;
    }
}