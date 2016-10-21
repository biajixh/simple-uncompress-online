package myservlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.lingala.zip4j.exception.ZipException;
import zip.*;


@SuppressWarnings("serial")
public class WebZipReader extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public WebZipReader() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

			doPost(request,response);
	}

	/**
	 * The doPost method of the servlet. <br>
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		String path = request.getParameter("path");
		List<Map<String, Object>> list1 = null;
		try {
			list1 = new ReadZip().readZipFile(path);
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<Map<String, Object>> list2 = new ReadZip().sortList(list1);
		List<Map<String, String>> pathlist = new ReadZip().pathlist(list2);
		request.setAttribute("list1", list2);
		request.setAttribute("path", path);
		request.setAttribute("list", pathlist);
		RequestDispatcher view = request.getRequestDispatcher("ZipMessage.jsp");
		view.forward(request,response);
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
