package by.fedyushkin.bloomly.util;

public final class PhoneNumbers {

    private PhoneNumbers() {
    }

    public static String normalize(String phone) {
        if (phone == null) {
            return null;
        }
        String digits = phone.replaceAll("[^0-9]", "");
        return digits.isBlank() ? null : digits;
    }
}
