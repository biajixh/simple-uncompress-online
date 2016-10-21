package myservlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.github.junrar.exception.RarException;

import rar.*;

public class URLRarReader extends HttpServlet {

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");
		String path = request.getParameter("path");
		try {
		List<Map<String, Object>> list1 = new ReadURLRar().readRarFile(path);
		List<Map<String, Object>> list2 = new ReadURLRar().sortList(list1);
		List<Map<String, String>> pathlist = new ReadURLRar().pathlist(list2);
		request.setAttribute("list1", list2);
		request.setAttribute("path", path);
		request.setAttribute("list", pathlist);//��list2�󶨵������������list��		
		} catch (RarException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		RequestDispatcher view = request.getRequestDispatcher("URLRarMessage.jsp");
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
