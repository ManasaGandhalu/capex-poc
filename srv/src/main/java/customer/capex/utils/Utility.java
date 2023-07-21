package customer.capex.utils;

import java.util.Random;

public class Utility {
    
    /**
     * generateToken
     * 
     * @param length
     * @return Generated Token
     */
    public static String generateToken(int length, boolean onlyNumbers) {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbers = "0123456789";
        String mixed = upper + numbers;

        if(onlyNumbers) {
            mixed = numbers;
        }
        
        // Using random method
        Random random = new Random();
        char[] token = new char[length];

        for (int i = 0; i < length; i++) {
            // Use of charAt() method : to get character value
            // Use of nextInt() as it is scanning the value as int
            token[i] = mixed.charAt(random.nextInt(mixed.length()));
        }
        String code = String.valueOf(token);
        if (code.startsWith("0")) {
            // generate again
            return generateToken(length, onlyNumbers);
        }
        return code;
    }

}