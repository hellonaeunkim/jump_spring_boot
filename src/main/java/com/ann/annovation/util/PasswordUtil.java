package com.ann.annovation.util;

import java.security.SecureRandom;

public class PasswordUtil {
    private static final char[] randomPasswordCharacters = new char[]{
            //number
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            //uppercase
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            //lowercase
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            //special symbols
            '@', '$', '!', '%', '*', '?', '&'
    };

    public static String getRandomPassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder();

        int randomPasswordCharactersLength = randomPasswordCharacters.length;
        for (int i = 0; i < length; i++) {
            stringBuilder.append(randomPasswordCharacters[random.nextInt(randomPasswordCharactersLength)]);
        }

        return stringBuilder.toString();
    }
}
