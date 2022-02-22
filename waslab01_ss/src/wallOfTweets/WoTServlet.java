package wallOfTweets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

/**
 * Servlet implementation class WoTServlet
 */
@WebServlet("/")
public class WoTServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Locale currentLocale = new Locale("en");
	String ENCODING = "ISO-8859-1";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public WoTServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			Vector<Tweet> tweets = Database.getTweets();
			if (request.getHeader("Accept").equals("text/plain")) printPLAINresult(tweets, request, response);
			else printHTMLresult(tweets, request, response);
		}
		catch (SQLException ex ) {
			throw new ServletException(ex);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String author = request.getParameter("author");
		String tweet_text = request.getParameter("tweet_text");
		/* Como hacemos para borrar un tweet
		 * En el cliente enviamos solo un parametro, el twid
		 * Desde la pagina enviamos con el boton el twid del tweet en cuestion
		 * A partir de si cuando extraemos el twid del request es nulo o no, identificamos si el cliente quiere borrar o no un tweet
		 */
		String twid = request.getParameter("twid");
		long num = -1;
		if (twid == null) {
			try {
				num = Database.insertTweet(author, tweet_text);
				response.addCookie(new Cookie(String.valueOf(num), getMd5(String.valueOf(num))));
			} catch (SQLException | NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (twid != null) { //Delete de un tweet
			if (request.getCookies().length != 0) {
				for (Cookie cookie: request.getCookies()) {
					try {
						if (cookie.getValue().equals(getMd5(twid))) Database.deleteTweet(Long.parseLong(twid));
					} catch (NumberFormatException | NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			//Para borrar tweets q no se pueden borrar con el boton
			//Database.deleteTweet(Long.parseLong(twid)); 
		}
		// This method does NOTHING but redirect to the main page
		if (request.getHeader("Accept").equals("text/plain")) {
			PrintWriter out = response.getWriter();
			out.print(num);
		}
		else response.sendRedirect(request.getContextPath()); //Actualiza
	}

	private void printHTMLresult (Vector<Tweet> tweets, HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.FULL, currentLocale);
		DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT, currentLocale);
		res.setContentType ("text/html");
		res.setCharacterEncoding(ENCODING);
		PrintWriter  out = res.getWriter ( );
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head><title>Wall of Tweets</title>");
		out.println("<link href=\"wallstyle.css\" rel=\"stylesheet\" type=\"text/css\" />");
		out.println("</head>");
		out.println("<body class=\"wallbody\">");
		out.println("<h1>Wall of Tweets</h1>");
		out.println("<div class=\"walltweet\">"); 
		out.println("<form method=\"post\">");
		out.println("<table border=0 cellpadding=2>");
		out.println("<tr><td>Your name:</td><td><input name=\"author\" type=\"text\" size=70></td><td></td></tr>");
		out.println("<tr><td>Your tweet:</td><td><textarea name=\"tweet_text\" rows=\"2\" cols=\"70\" wrap></textarea></td>"); 
		out.println("<td><input type=\"submit\" name=\"action\" value=\"Tweet!\"></td></tr>"); 
		out.println("</table></form></div>");
		String currentDate = "None";
		for (Tweet tweet: tweets) {
			String messDate = dateFormatter.format(tweet.getDate());
			if (!currentDate.equals(messDate)) {
				out.println("<br><h3>...... " + messDate + "</h3>");
				currentDate = messDate;
			}
			out.println("<div class=\"wallitem\">");
			out.println("<form method=\"post\">");
			out.println("<tr><h4><em>" + tweet.getAuthor() + "</em> @ "+ timeFormatter.format(tweet.getDate()) +"</h4>");
			out.println("<p>" + tweet.getText() + "</p>");	
			/* Insert del boton
			 * Boton tipo submit porque queremos enviar una informacion
			 * Como lo que identifica un tweet es su twid debemos hacer q en el formulario enviado esté su twid
			 * He descubierto que se puede añadir un parametro a un formulario pero que no se muestre en la pagina
			 * -> input type=\"hidden\"
			 */
			out.println("<td><input type=\"submit\" name=\"action\" value=\"Borrar Tweet\"></td></tr>"); 
			out.println("<tr><td><input type=\"hidden\" name=\"twid\" value=" + tweet.getTwid() + "></td></tr>");
			out.println("</form>");
			out.println("</div>");
		}
		out.println ( "</body></html>" );
	}
	
	private void printPLAINresult (Vector<Tweet> tweets, HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		PrintWriter  out = res.getWriter ( );
		for (Tweet tweet: tweets) {
			out.println("tweet #" + tweet.getTwid() + ": " + tweet.getAuthor() + ": " + tweet.getText() + " [" + tweet.getDate() + "]");
		}
	}
	
	public static String getMd5(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
		MessageDigest md = MessageDigest.getInstance("MD5");
	    md.update(input.getBytes("UTF-8"));
	    return String.format("%032x", new BigInteger(1, md.digest()));
    }
}
