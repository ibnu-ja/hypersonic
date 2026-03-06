project_id = 'io.ibnuja.Hypersonic'
pkgdatadir = get_option('datadir') / meson.project_name()
gschema_dir = get_option('datadir') / 'glib-2.0' / 'schemas'
i18n = import('i18n')

desktop_file = i18n.merge_file(
        input: project_id + '.desktop.in',
       output: project_id + '.desktop',
         type: 'desktop',
       po_dir: '../po',
      install: true,
  install_dir: get_option('datadir') / 'applications'
)

desktop_utils = find_program('desktop-file-validate', required: false)
if desktop_utils.found()
  test('Validate desktop file', desktop_utils, args: [desktop_file])
endif

appstream_file = i18n.merge_file(
        input: project_id + '.metainfo.xml.in',
       output: project_id + '.metainfo.xml',
       po_dir: '../po',
      install: true,
  install_dir: get_option('datadir') / 'metainfo'
)

appstreamcli = find_program('appstreamcli', required: false, disabler: true)
if appstreamcli.found()
  test('Validate appstream file', appstreamcli,
     args: ['validate', '--no-net', '--explain', appstream_file])
endif

install_data(project_id + '.gschema.xml',
  install_dir: gschema_dir
)

gnome.compile_schemas(
  build_by_default: true,
  depend_files: project_id + '.gschema.xml'
)

compile_schemas = find_program('glib-compile-schemas', required: false, disabler: true)
if compile_schemas.found()
  test('Validate schema file',
     compile_schemas,
     args: ['--strict', '--dry-run', meson.current_source_dir()])
endif

service_conf = configuration_data()
service_conf.set('bindir', get_option('prefix') / get_option('bindir'))
configure_file(
  input: project_id + '.service.in',
  output: project_id + '.service',
  configuration: service_conf,
  install_dir: get_option('datadir') / 'dbus-1' / 'services'
)

subdir('icons')
