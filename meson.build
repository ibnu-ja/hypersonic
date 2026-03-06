project(
  'hypersonic',
  'c',
  version: '1.0-SNAPSHOT',
  meson_version: '>= 1.0.0',
  default_options: [ 'warning_level=2', 'werror=false' ],
)

i18n = import('i18n')
gnome = import('gnome')

shadow_dist_dir = join_paths(meson.project_source_root(), 'build', 'install', meson.project_name() + '-shadow')

jar_name = meson.project_name() + '-' + meson.project_version() + '-all.jar'
jar_path = join_paths(shadow_dist_dir, 'lib', jar_name)

script_name = meson.project_name()
if host_machine.system() == 'windows'
  script_name += '.bat'
endif
script_path = join_paths(shadow_dist_dir, 'bin', script_name)

app_datadir = get_option('datadir') / meson.project_name()

config_h = configuration_data()
config_h.set_quoted('PACKAGE_VERSION', meson.project_version())
config_h.set_quoted('GETTEXT_PACKAGE', 'hypersonic')
config_h.set_quoted('LOCALEDIR', get_option('prefix') / get_option('localedir'))
config_h.set_quoted('DATADIR', get_option('prefix') / app_datadir)
configure_file(output: 'config.h', configuration: config_h)

install_data(
  jar_path,
  install_dir: app_datadir
)

install_data(
  script_path,
  install_dir: get_option('bindir'),
  install_mode: 'rwxr-xr-x'
)

subdir('data')
subdir('po')

gnome.post_install(
  glib_compile_schemas: true,
  gtk_update_icon_cache: true,
  update_desktop_database: true,
)
