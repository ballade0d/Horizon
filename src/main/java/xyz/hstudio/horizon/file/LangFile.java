package xyz.hstudio.horizon.file;

import xyz.hstudio.horizon.util.wrap.YamlLoader;

public class LangFile extends AbstractFile {

    @Load(path = "verbose")
    public String verbose;

    @Override
    public Object getValue(final String path, final YamlLoader loader, final Class<?> type) {
        return loader.get(path);
    }
}