# Hypersonic

Subsonic client made with Java and GTK4+Libadwaita.
### Linux

**Development:**
```bash
meson setup builddir
meson compile -C builddir
./gradlew run -PmesonPrefix=builddir
```

**Install (system-wide):**
```bash
./gradlew installShadowDist -PmesonPrefix=/usr/local -PmesonDatadir=/usr/local/share/hypersonic
meson setup --prefix=/usr/local builddir
meson compile -C builddir
sudo meson install -C builddir
```

**Install (user):**
```bash
./gradlew installShadowDist -PmesonPrefix=~/.local -PmesonDatadir=~/.local/share/hypersonic
meson setup --prefix=~/.local builddir
meson compile -C builddir
meson install -C builddir
```

***

### Windows

**Development:**
```cmd
meson setup builddir
meson compile -C builddir
gradlew.bat run -PmesonPrefix=builddir
```

**Install:**
```cmd
gradlew.bat installShadowDist -PmesonPrefix=%APPDATA%/hypersonic -PmesonDatadir=%APPDATA%/hypersonic/share/hypersonic
meson setup --prefix=%APPDATA%/hypersonic builddir
meson compile -C builddir
meson install -C builddir
```
