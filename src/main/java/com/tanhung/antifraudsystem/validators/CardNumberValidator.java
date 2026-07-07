package com.tanhung.antifraudsystem.validators;

public final class CardNumberValidator {
    private CardNumberValidator(){
        throw new UnsupportedOperationException("Utility class - do not instantiate");
    }
    public static boolean isValidCardNumber(String number){
        if(number == null || number.isBlank() || number.length() != 16) return false;
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
        return ((10 - (sum % 10)) % 10) == checkNum;
    }
}
