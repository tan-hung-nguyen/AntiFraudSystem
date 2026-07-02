package com.tanhung.antifraudsystem.validator;

public interface CardNumberValidator {
    static boolean isValidCardNumber(String number){

        int sum = 0;
        int checkNum = number.charAt(number.length() - 1) - '0';
        boolean alternate = true;
        for (int i = number.length() - 2; i >= 0; --i) {
            int n = number.charAt(i) - '0';

            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % checkNum == 0;
    };
}
