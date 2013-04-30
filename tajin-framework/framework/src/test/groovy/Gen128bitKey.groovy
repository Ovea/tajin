/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-29
 */
class Gen128bitKey {
    public static void main(String[] args) {
        println new BigInteger(128, new Random()).toString(16)
    }
}
