# jruby scripting container
jar 'org.jruby:jruby-complete', '@project.version@'

# unit tests
jar 'junit:junit', '4.8.2', :scope => :test

plugin :surefire, '2.15', :additionalClasspathElements => [ '${basedir}/../../../../../core/target/test-classes', '${basedir}/../../../../../test/target/test-classes' ]
