package lightbot;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class ModuleLoader {
	
	private List<URL> moduleURLs = new ArrayList<URL>();
	
	public List<Module> loadAllModules(File file) {
		List<Module> modules = new ArrayList<Module>();
		if(file.isDirectory()) {
			try {
				URLClassLoader loader = new URLClassLoader(new URL[] {file.toURI().toURL()});
				String basePath = file.getPath() + file.separator;
				for(File f : file.listFiles()) {
					if(f.isDirectory())
						modules.addAll(loadAllModules(f));
					else if(!f.getName().contains("$"))
						try {
							String fPackage = f.getPath().replace(basePath, "");
							Class c = loader.loadClass("lightbot.modules." + f.getName().replaceAll("\\" + file.separator, ".").replaceFirst("\\.[Cc][Ll][Aa][Ss][Ss]$", ""));
							Module m = (Module) c.newInstance();
							modules.add(m);
							System.out.println("Loaded module: " + m.getName() + " (" + m.getVersion() + ")");
						}
						catch (ClassNotFoundException e) {
							e.printStackTrace();
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return modules;
	}
}
