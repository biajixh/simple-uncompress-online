<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>zip与rar</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->

  </head>
  
  <body marginheight="0" marginwidth="0" bgcolor="white">
    	<div style="text-align: center;padding-top: 40px;padding-bottom: 40px;font-family: 微软雅黑;font-size: 28px">zip与rar文件预览解压</div>
    	<div style="width: 25%;border:1px solid #000;float: left;height: 50%;padding-top: 80px" align="center">
    		<ul style="font-family: 微软雅黑;font-size: 24px">小组成员</ul>
    		<ul style="font-family: 微软雅黑;list-style: none;">
    			<li>吴嘉茜</li>
    			<li>柯伟孟</li>
    			<li>赵引</li>
    			<li>许昊</li>
    			<li>龙亚辉</li>
    			<li>李俊桦</li>
    		</ul>
    	</div>
    	<div style="width: 100%;border-top: 1px solid #000;padding-top: 184px;padding-bottom: 196px;font-size: 30px;" align="center">
    		<a href="zip.jsp" style="text-decoration: none;" target="_blank">zip传送门</a>&nbsp|&nbsp
    		<a href="rar.jsp" style="text-decoration: none;" target="_blank">rar传送门</a>
    	</div>
    	<div style="width: 100%;height: 100px;border-top:1px solid #000;clear: both" align="center">
    		<p align="center" style="font-family: 微软雅黑;">天翼云在线解压缩文件</p>
    		<P align="center" style="font-family: 微软雅黑;">&copy; 2016 - 至今  在线解压缩1组 </P>
    	</div>
  </body>
</html>
