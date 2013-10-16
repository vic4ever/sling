package org.apache.sling.slingbucks.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Mnemo {

    private static int ID = 0;

    protected char[] consonants = new char[]{
            'b', 'd', 'g', 'h', 'j', 'k', 'm',
            'n', 'p', 'r', 's', 't', 'z'};

    /**
     * @var array Vowels used in Mnemo words
     */
    protected char[] vowels = new char[]{
            'a', 'e', 'i', 'o', 'u'
    };

    /**
     * @var array Additional syllables used in Mnemo words
     */
    protected String[] additionals = new String[]{
            "wa", "wo", "ya", "yo", "yu"
    };

    protected Map<String,String> special;

    /**
     * @var string Syllable representing negative values, used as a prefix
     */
    protected String negative = "wi";

    /**
     * @var array Syllables mapping
     */
    protected List<String> syllables;

    
    private static Mnemo instance = null;
    
    /**
     * Builds the internal list of syllables.
     *
     * @return void
     */
    protected Mnemo()
    {
        special = new HashMap<String, String>();
        special.put("hu", "fu");
        special.put("si", "shi");
        special.put("ti", "chi");
        special.put("tu", "tsu");
        special.put("zi", "tzu");

        syllables = new ArrayList<String>();

        for(char consonant: this.consonants){
            for(char vowel: this.vowels){
                syllables.add(consonant + "" + vowel);
            }
        }

        for(String additional: this.additionals){
            syllables.add(additional);
        }
    }
    
    public static Mnemo getInstance() {
          if(instance == null) {
             instance = new Mnemo();
          }
          return instance;
       }

    /**
     * Turns the given integer into a Mnemo word.
     *
     * @param integer $integer Integer to turn into a word.
     *
     * @return string The equivalent Mnemo word.
     */
    public String fromInteger(int integer)
    {

        if (integer < 0) {
            return this.negative + "" +  this.fromInteger(Math.abs(integer));
        }

        return this.toSpecial(this.fromIntegerInner(integer) + ID);
    }


    /**
     * Alias of {@see Mnemo::fromInteger()}.
     *
     * @param $integer Integer to turn into a word.
     *
     * @return string The equivalent Mnemo word.
     */
    public String toString(int integer)
    {
        return this.fromInteger(integer);
    }

    /**
     * The actual recursive function that turns an integer into its
     * equivalent Mnemo word.
     *
     * @param integer $integer Integer to turn into a word.
     *
     * @return string The equivalent Mnemo word.
     */
    protected String fromIntegerInner(int integer){
        if (integer == 0) {
            return "";
        }

        int mod = integer % this.syllables.size();
        int rest = (int) Math.floor(integer / this.syllables.size());

        return this.fromIntegerInner(rest)+ "" + this.syllables.get(mod);
    }

    /**
     * Replaces several special syllables in a word using a built-in mapping.
     *
     * @param string $string String to transform
     *
     * @return string String with special syllables replaced
     */
    protected String toSpecial(String string)
    {
        String retVal = string;
        for(Entry<String,String> entry : this.special.entrySet()){
            String val = entry.getValue();
            String key = entry.getKey();
            retVal = retVal.replace(key, val);
        }
        return retVal;
    }

    /**
     * Returns a word with the original syllables restored.
     *
     * @param string $string String to transform
     *
     * @return string String with original syllables restored
     */
    protected String fromSpecial(String string)
    {
        String retVal = string;
        for(Entry<String,String> entry : this.special.entrySet()){
            String val = entry.getValue();
            String key = entry.getKey();
            retVal = retVal.replace(val, key);
        }
        return retVal;
    }
}