package com.intuitive.etl.utils;

public class ValidationUtils {
    public static boolean isCnpjValid(String cnpj) {
        if (cnpj == null) return false;

        String num = cnpj.replaceAll("\\D", "");
        if (num.length() != 14) return false;
        if (num.matches("(\\d)\\1{13}")) return false;

        try {
            int soma = 0, resto;
            int[] peso1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int[] peso2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

            for (int i = 0; i < 12; i++) soma += (num.charAt(i) - '0') * peso1[i];
            resto = soma % 11;
            char dig1 = (resto < 2) ? '0' : (char) ((11 - resto) + '0');

            soma = 0;
            for (int i = 0; i < 13; i++) soma += (num.charAt(i) - '0') * peso2[i];
            resto = soma % 11;
            char dig2 = (resto < 2) ? '0' : (char) ((11 - resto) + '0');

            return (dig1 == num.charAt(12) && dig2 == num.charAt(13));
        } catch (Exception e) {
            return false;
        }
    }
}
