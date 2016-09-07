package jetty;

	import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

	import org.eclipse.jetty.jsp.JettyJspServlet;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.JavaUtilLog;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.WebAppContext;

	/**
	 * Example of using JSP's with embedded jetty and not requiring
	 * all of the overhead of a WebAppContext
	 */
	public class Main
	{
		
		
		// Resource path pointing to where the WEBROOT is
	    private static final String WEBAPP_INDEX = "/webapp";
	    public static String DIR_TOPIC_GRAPHS = null;
	    
	    public static void main(String[] args) throws Exception
	    {
			CommandLineParser clParser = new BasicParser();
			Option portOption = Option.builder()
					.longOpt("port")
					.numberOfArgs(1)
					.required(false)
					.type(Integer.class)
					.desc("server port, default 8080")
					.build();
			Option graphDirOption = Option.builder()
					.longOpt("dirGraph")
					.numberOfArgs(1)
					.required(false)
					.type(String.class)
					.desc("path to directory with graph representations of topics. Default: /home/simondif/data-webapp/topics")
					.build();
			Options options = new Options();
			options.addOption(portOption);
			options.addOption(graphDirOption);

			CommandLine cl = null;
			boolean success = false;
			try {
				cl = clParser.parse(options, args);
				success = true;
			} catch (ParseException e) {
				System.out.println(e.getMessage());
			}
			if (!success) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("java -jar ***.jar ", options, true);
				System.exit(1);
			}
			int port = cl.hasOption("port") ? Integer.valueOf(cl.getOptionValue("port")) : 8080;
	        DIR_TOPIC_GRAPHS = cl.hasOption("dirGraph") ? cl.getOptionValue("dirGraph") : "/home/simondif/data-webapp/topics";
	        Main main = new Main(port);
	        main.start();
	        main.waitForInterrupt();
	    }

	    private static final Logger LOG = Logger.getLogger(Main.class.getName());

	    private int port;
	    private Server server;
	    private URI serverURI;

	    public Main(int port)
	    {
	        this.port = port;
	    }

	    public URI getServerURI()
	    {
	        return serverURI;
	    }

	    public void start() throws Exception
	    {
	        server = new Server();
	        ServerConnector connector = connector();
	        server.addConnector(connector);

	        URI baseUri = getWebRootResourceUri();

	        // Set JSP to use Standard JavaC always
	        System.setProperty("org.apache.jasper.compiler.disablejsr199", "false");

	        WebAppContext webAppContext = getWebAppContext(baseUri, getScratchDir());

	        server.setHandler(webAppContext);

	        // Start Server
	        server.start();

	        // Show server state
	        if (LOG.isLoggable(Level.FINE))
	        {
	            LOG.fine(server.dump());
	        }
	        this.serverURI = getServerUri(connector);
	    }

	    private ServerConnector connector()
	    {
	        ServerConnector connector = new ServerConnector(server);
	        connector.setPort(port);
	        return connector;
	    }

	    private URI getWebRootResourceUri() throws FileNotFoundException, URISyntaxException
	    {
	        URL indexUri = Main.class.getResource(WEBAPP_INDEX);
	        if (indexUri == null)
	        {
	        	throw new FileNotFoundException("Unable to find resource " + WEBAPP_INDEX);
	        }
	        return indexUri.toURI();
	    }

	    /**
	     * Establish Scratch directory for the servlet context (used by JSP compilation)
	     */
	    private File getScratchDir() throws IOException
	    {
	        File tempDir = new File(System.getProperty("java.io.tmpdir"));
	        File scratchDir = new File(tempDir.toString(), "embedded-jetty-jsp");

	        if (!scratchDir.exists())
	        {
	            if (!scratchDir.mkdirs())
	            {
	                throw new IOException("Unable to create scratch directory: " + scratchDir);
	            }
	        }
	        return scratchDir;
	    }

	    /**
	     * Setup the basic application "context" for this application at "/"
	     * This is also known as the handler tree (in jetty speak)
	     */
	    private WebAppContext getWebAppContext(URI baseUri, File scratchDir)
	    {
	        WebAppContext context = new WebAppContext();
	        context.setContextPath("/");
	        context.setAttribute("javax.servlet.context.tempdir", scratchDir);
	        context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
	          ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/.*taglibs.*\\.jar$");
	        context.setResourceBase(baseUri.toASCIIString());
	        context.setAttribute("org.eclipse.jetty.containerInitializers", jspInitializers());
	        context.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
	        context.addBean(new ServletContainerInitializersStarter(context), true);
	        context.setClassLoader(getUrlClassLoader());

	        context.addServlet(jspServletHolder(), "*.jsp");
	        // Add Application Servlets

	        context.addServlet(exampleJspFileMappedServletHolder(), "/test/foo/");
	        context.addServlet(defaultServletHolder(baseUri), "/");
	        return context;
	    }

	    /**
	     * Ensure the jsp engine is initialized correctly
	     */
	    private List<ContainerInitializer> jspInitializers()
	    {
	        JettyJasperInitializer sci = new JettyJasperInitializer();
	        ContainerInitializer initializer = new ContainerInitializer(sci, null);
	        List<ContainerInitializer> initializers = new ArrayList<ContainerInitializer>();
	        initializers.add(initializer);
	        return initializers;
	    }

	    /**
	     * Set Classloader of Context to be sane (needed for JSTL)
	     * JSP requires a non-System classloader, this simply wraps the
	     * embedded System classloader in a way that makes it suitable
	     * for JSP to use
	     */
	    private ClassLoader getUrlClassLoader()
	    {
	        ClassLoader jspClassLoader = new URLClassLoader(new URL[0], this.getClass().getClassLoader());
	        return jspClassLoader;
	    }

	    /**
	     * Create JSP Servlet (must be named "jsp")
	     */
	    private ServletHolder jspServletHolder()
	    {
	        ServletHolder holderJsp = new ServletHolder("jsp", JettyJspServlet.class);
	        holderJsp.setInitOrder(0);
	        holderJsp.setInitParameter("logVerbosityLevel", "DEBUG");
	        holderJsp.setInitParameter("fork", "false");
	        holderJsp.setInitParameter("xpoweredBy", "false");
	        holderJsp.setInitParameter("compilerTargetVM", "1.7");
	        holderJsp.setInitParameter("compilerSourceVM", "1.7");
	        holderJsp.setInitParameter("keepgenerated", "true");
	        return holderJsp;
	    }

	    /**
	     * Create Example of mapping jsp to path spec
	     */
	    private ServletHolder exampleJspFileMappedServletHolder()
	    {
	        ServletHolder holderAltMapping = new ServletHolder();
	        holderAltMapping.setName("foo.jsp");
	        holderAltMapping.setForcedPath("/test/foo/foo.jsp");
	        return holderAltMapping;
	    }

	    /**
	     * Create Default Servlet (must be named "default")
	     */
	    private ServletHolder defaultServletHolder(URI baseUri)
	    {
	        ServletHolder holderDefault = new ServletHolder("default", DefaultServlet.class);
	        LOG.info("Base URI: " + baseUri);
	        holderDefault.setInitParameter("resourceBase", baseUri.toASCIIString());
	        holderDefault.setInitParameter("dirAllowed", "true");
	        return holderDefault;
	    }

	    /**
	     * Establish the Server URI
	     */
	    private URI getServerUri(ServerConnector connector) throws URISyntaxException
	    {
	        String scheme = "http";
	        for (ConnectionFactory connectFactory : connector.getConnectionFactories())
	        {
	            if (connectFactory.getProtocol().equals("SSL-http"))
	            {
	                scheme = "https";
	            }
	        }
	        String host = connector.getHost();
	        if (host == null)
	        {
	            host = "localhost";
	        }
	        int port = connector.getLocalPort();
	        serverURI = new URI(String.format("%s://%s:%d/", scheme, host, port));
	        LOG.info("Server URI: " + serverURI);
	        return serverURI;
	    }

	    public void stop() throws Exception
	    {
	        server.stop();
	    }

	    /**
	     * Cause server to keep running until it receives a Interrupt.
	     * <p>
	     * Interrupt Signal, or SIGINT (Unix Signal), is typically seen as a result of a kill -TERM {pid} or Ctrl+C
	     * @throws InterruptedException if interrupted
	     */
	    public void waitForInterrupt() throws InterruptedException
	    {
	        server.join();
	    }

}
