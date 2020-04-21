package xyz.hstudio.horizon.file.node;

import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.CheckFile;
import xyz.hstudio.horizon.file.Load;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

public class AutoClickerNode extends CheckFile {

    // TypeA
    @Load(path = "typeA.enabled")
    public boolean typeA_enabled = true;
    @Load(path = "typeA.min_encounter_cps")
    public double typeA_min_encounter_cps = 6.0;
    @Load(path = "typeA.min_check_cps")
    public double typeA_min_check_cps = 9.0;
    @Load(path = "typeA.sample_size")
    public int typeA_sample_size = 20;
    @Load(path = "typeA.samples")
    public int typeA_samples = 5;
    @Load(path = "typeA.stdev")
    public double typeA_stdev = 0.7;

    public AutoClickerNode load(final String pathPrefix, final YamlLoader loader) {
        return AbstractFile.load(pathPrefix, this, loader);
    }
}