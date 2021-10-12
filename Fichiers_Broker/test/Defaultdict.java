package test;
import java.util.concurrent.ConcurrentHashMap;

public class Defaultdict<K, V> extends ConcurrentHashMap<K, V> {

    Class<V> klass;
    public Defaultdict(Class klass) {
        this.klass = klass;
    }

    @Override
    public V get(Object key) {
        V returnValue = super.get(key);
        if (returnValue == null) {
            try {
                returnValue = klass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            this.put((K) key, returnValue);
        }
        return returnValue;
    }
}