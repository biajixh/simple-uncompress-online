<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>Read Zip</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->
  </head>
  
  <body>
  	<form action="WebZipReader" method="post"> 
  		输入zip文件的本地路径：<input type="text" name="path">
  		<input type="submit" value="提交">
  	</form>
  	<br>
  	<form action="URLZipReader" method="post">
  		输入zip文件的URL地址:<input type="text" name="path">
  		<input type="submit" value="提交">
  	</form>
  	<a href="rar.jsp" target="_blank">rar文件解压传送！</a>
  </body>
</html>
