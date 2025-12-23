package com.example.dbnew.context;

public final class DynamicDataSourceContextHolder {

    private static final ThreadLocal<DataSourceKey> CONTEXT = ThreadLocal.withInitial(() -> DataSourceKey.MYSQL);

    private DynamicDataSourceContextHolder() {
    }

    public static void setDataSource(DataSourceKey key) {
        if (key != null) {
            CONTEXT.set(key);
        }
    }

    public static DataSourceKey getDataSource() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
