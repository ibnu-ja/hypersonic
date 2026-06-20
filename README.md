# Hypersonic

Subsonic client made with Java, GTK4, and Libadwaita. The project is set up to be built and installed as a Flatpak application.

---

## Installation instructions

1. Clone this repository into a local folder.

2. Install the GNOME 50 Sdk and Platform runtime:

    ```bash
    flatpak --user install org.gnome.Platform//50 org.gnome.Sdk//50
    ```

3. Install the OpenJDK flatpak extension:

    ```bash
    flatpak --user install org.freedesktop.Sdk.Extension.openjdk25//25.08
    ```

4. Build and install the application:

    ```bash
    flatpak-builder --force-clean \
                    --user \
                    --install \
                    --state-dir=/tmp/flatpak-builder \
                    /tmp/builddir \
                    moe.seiga.Hypersonic.Devel.yml
    ```

    The `--force-clean` option will delete previous build files, and `--user` will install the app for the current user instead of a system-wide installation.

    The `--state-dir` option changes the state directory of flatpak-builder from `.flatpak-builder` to a directory in `/tmp`, to prevent IntelliJ IDEA from indexing its contents, which can significantly slow down or freeze the IDE.

    The `/tmp/builddir` location can be set to any other directory of your choosing, as long as it is on the same filesystem as the flatpak-builder state directory.

5. Run the application:

    ```bash
    flatpak run moe.seiga.Hypersonic.Devel
    ```

---

## Developing the application

You can open the project folder in IntelliJ IDEA, and it will automatically load the Gradle project. To run the application from the command line or terminal:

```bash
./gradlew run
```

### Blueprint files

Blueprint (.blp) files are located in `src/main/blueprints`. 

When you change them, they must be compiled to XML (.ui) format with `blueprint-compiler`. The output .ui files are then compiled into a gresource bundle with `glib-compile-resources`.

The Gradle build script contains the `compileBlueprints` and `compileGResources` tasks to automate this. Running `./gradlew compileGResources` (or starting the app via `./gradlew run`) compiles the blueprints and builds the gresource bundle in the `build/` directory automatically.

When you create a new Blueprint file, add the filename in the following locations:
* `src/main/gresources/hypersonicapp.gresource.xml` (change the extension from `.blp` to `.ui`)
* `po/POTFILES.in` if the file contains translatable strings.

### Translations

The application is translated with GNU gettext. 

All files containing translatable strings must be added to `po/POTFILES.in`.

To extract new translatable strings and update the `hypersonic.pot` template file, run:
```bash
./gradlew extractPot
```

To merge template updates into a specific language's `.po` file (e.g., `id.po`):
```bash
./gradlew mergePo_id
```

During build or installation, the Gradle plugin automatically runs `compileMo_<lang>` to compile the `.po` files into binary `.mo` files and stages them under the correct locale directory.

---

## Uninstalling

Run the following command to remove the locally installed development application:

```bash
flatpak uninstall moe.seiga.Hypersonic.Devel
```
