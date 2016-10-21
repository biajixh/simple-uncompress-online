package myservlet;

import java.io.IOException;
import java.io.PrintWriter;

import rar.*;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



@SuppressWarnings("serial")
public class SelectUnrarFile extends HttpServlet {

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

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	try{
		request.setCharacterEncoding("utf-8");
		UnRar ur = new UnRar();
		response.setContentType("text/html");
		String[] values = request.getParameterValues("path");
		String outpath = request.getParameter("UnZipPath");
		String zippath = request.getParameter("zipname");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>result</TITLE></HEAD>");
		out.println("  <BODY>");	
		if(outpath.equals("")||zippath.equals("")){
			out.println("Please select yes and input unzippath");
			}
		else{
			for(String value:values){
				ur.unRarFile(zippath, outpath, value);
				out.println("finished!");
			}
		out.println("  </BODY>");
		out.println("</HTML>");
		out.close();
		}
	}catch (Exception e) {
		// TODO: handle exception
		e.printStackTrace();
	}
		
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
