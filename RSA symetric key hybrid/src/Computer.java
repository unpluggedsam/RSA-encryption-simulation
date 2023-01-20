import java.util.ArrayList;
import java.util.List;
import java.math.BigInteger;

public class Computer {

    private final List<String> recentMessages = new ArrayList<>();
    private final BigInteger publicKey;
    private final BigInteger privateKey;
    private final BigInteger mod;

    public Computer() {
        Key key = new Key();
        this.publicKey = key.getPublicKey();
        this.privateKey = key.getPrivateKey();
        this.mod = key.getModulus();
    }

    public Packet sendMessage(Computer computer, String message, OneTimePad.KEY_SIZE size) {
        OneTimePad pad = new OneTimePad();
        int[] key = pad.generateKey(size);
        BigInteger encryptedDecimalKey = convertKeyToDecimalAndEncrypt(key, computer);
        BigInteger encryptedSize = cipher(BigInteger.valueOf(size.getSize()), computer.getPublicKey(), computer.getMod());
        Packet packet = new Packet(OneTimePad.encryptString(message, key), encryptedDecimalKey, encryptedSize);
        computer.receiveMessage(packet);
        return packet;
    }

    public void receiveMessage(Packet packet) {
        String decryptedMessage = OneTimePad.decryptBinary(packet.getBinary(), convertKeyToBinaryAndDecrypt(packet.getKey(), OneTimePad.KEY_SIZE.valueOf(cipher(packet.getKeySize(), this.privateKey, this.mod).intValue()).get()));
        recentMessages.add(decryptedMessage);
    }

    public List<String> getRecentMessages() {
        return recentMessages;
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }

    public BigInteger getMod() {
        return mod;
    }

    private BigInteger convertKeyToDecimalAndEncrypt(int[] key, Computer computer) {
        return cipher(convertBinaryToDecimal(key), computer.getPublicKey(), computer.getMod());
    }

    private int[] convertKeyToBinaryAndDecrypt(BigInteger key, OneTimePad.KEY_SIZE size) {
        return convertDecimalToBinary(cipher(key, this.privateKey, this.mod), size.getSize());
    }

    private BigInteger cipher(BigInteger binaryKeyAsDecimal, BigInteger key, BigInteger modulus) {
        return binaryKeyAsDecimal.modPow(key, modulus);
    }

    private BigInteger convertBinaryToDecimal(int[] binary) {
        BigInteger decimal = BigInteger.ZERO;
        BigInteger two = BigInteger.TWO;
        for (int i = 0; i < binary.length; i++) {
            decimal = decimal.add(BigInteger.valueOf(binary[i]).multiply(two.pow(binary.length - i - 1)));
        }
        return decimal;
    }

    private int[] convertDecimalToBinary(BigInteger decimal, int keySize) {
        ArrayList<Integer> binaryList = new ArrayList<>();
        while (!decimal.equals(BigInteger.ZERO)) {
            BigInteger[] divideAndRemainder = decimal.divideAndRemainder(BigInteger.TWO);
            binaryList.add(divideAndRemainder[1].intValue());
            decimal = divideAndRemainder[0];
        }

        int[] binary = new int[binaryList.size()];
        for (int i = 0; i < binary.length; i++) {
            binary[i] = binaryList.get(binary.length - 1 - i);
        }

        if (binary.length < keySize) {
            int[] newBinary = new int[keySize];
            int difference = keySize - binary.length;
            for (int i = 0; i < difference; i++) {
                newBinary[i] = 0;
            }
            if (keySize - difference >= 0)
                System.arraycopy(binary, 0, newBinary, difference, keySize - difference);
            binary = newBinary;
        }
        return binary;
    }
}
