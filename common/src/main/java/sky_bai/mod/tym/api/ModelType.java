package sky_bai.mod.tym.api;

public class ModelType<T> {
    final boolean b;
    final T t;

    private ModelType(T t, boolean b){
        this.t = t;
        this.b = b;
    }

    public boolean is() {
        return b;
    }

    public T get() {
        return t;
    }


    public static <T> ModelType.Builder<T> builder(){
        return new ModelType.Builder<>();
    }

    public static class Builder<T>{
        boolean b = false;
        T t = null;

        public ModelType<T> build(){
            return new ModelType<T>(t,b);
        }

        public ModelType.Builder<T> set(T t) {
            b = true;
            this.t = t;
            return this;
        }

        public ModelType.Builder<T> reset() {
            b = false;
            t = null;
            return this;
        }

    }

}
