package com.example.topyouth.utility_classes;

import androidx.annotation.NonNull;

/**
 * @author Mohamed Msaad
 * free to use it and modify if needed.
 * **/
public  class PasswordClassStuff {
    private static final String TAG = "PasswordClassStuff";
    private static final char[] specialChars = {'@', '#', '$', '%', '^', '&', '*', '-', '_', '!','+', '=', '[',']', '{','}', '|', '\\', ':', '\'' ,',','.','?','/','`','~','\"','(',')',';'};



    /**
     * This method checks if the password contains any digit character
     * @param input : password input
     * @return boolean true or false
     *
     * **/
    public static boolean hasDigits(@NonNull String input) {
        boolean has = false;
        char[] spli = input.toCharArray();
        for (char c : spli) {
            if (Character.isDigit(c))
                has = true;
        }
        return has;
    }

    /**
     * Method checks if the password is long enough
     * @param input: password string
     *
     * **/
    public static boolean isLongEnough(@NonNull String input) {
        return input.length() >= 10 && input.length() <32;
    }

    /**
     * checks if the password contains an Special character
     * @param input: password string
     * @return boolean true or false
     * **/
    public static boolean hasSpecial(@NonNull String input) {
        //todo continue
        boolean has = false;
        char spli[] = input.toCharArray();
        for (int i = 0; i < spli.length; i++) {
            for (int j = 0; j < specialChars.length; j++)
                if (specialChars[j]==(spli[i])) {
                    has = true;
                }
        }
        return has;
    }
    /**
     * checks if the password contains an uppercase character
     * @param input: password string
     * @return boolean true or false
     * **/
    public static boolean hasUpperCase(String input) {
        int i;
        boolean has = false;
        //todo continue
        char spli[] = input.toCharArray();
        for (i = 0; i < input.length(); i++)
            if (Character.isUpperCase(spli[i])) {
                has = true;
            }
        return has;
    }
}
