build_dir = ./build
pkg_dir = ./build/plugin
tomcat_url="file:///Users/spike/Downloads/apache-tomcat-7.0.29.zip"
tomcat_md5=i3a1fd1825202631e6c43461fa018c4f6

compile:
	mkdir -p $(build_dir)

	@if [ -e tomcat7 ]; then \
	   echo "Skipping Tomcat download"; \
	else \
	   echo "Downloading Tomcat..."; \
	   curl $(tomcat_url) > $(build_dir)/tomcat.zip; \
	   unzip -d . $(build_dir)/tomcat.zip; \
	   mv ./apache-tomcat-7* tomcat7; \
	   rm -rf tomcat7/webapps; \
	fi

package: compile
	jar cvf $(build_dir)/tomcat7-plugin.zip control server setup tomcat7

clean:
	rm -rf $(build_dir)
	rm -rf tomcat7

