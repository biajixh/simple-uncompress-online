<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>rar messages</title>
</head>
<body>
<form action="URLRarSelect" method="post">
压缩文件路径："${path}"<br><br>
是否解压？<br>
<input type="radio" value="${path}" name="zipname">yes   
<input type="radio" value="" name="zipname">no<br><br>
解压路径：<input type="text" name="UnZipPath">
<input type="submit" value="submit"><br><br>
选择解压文件：
<br>
<table align="left">
	<tr valign="top">
		<td align="right">
		<br>
			<c:forEach items="${list}" var="map" ><!--遍历list里的map元素并形成复选框-->
				<c:forEach items="${map}" var="entry">
					<label><input name="path" type="checkbox" value="${entry.value}"></label>
					<br><br><br><br><br><br><br><br><br>
				</c:forEach>
			</c:forEach>
		</td>
		<td align="left">
			<c:forEach items="${list1}" var="map1" ><!--遍历list1里的map1元素-->
				<c:forEach items="${map1}" var="entry1"><br>
					${entry1.key}:${entry1.value} 
				</c:forEach><br>
			</c:forEach>		
		</td>
	<tr>
</table>
</form>
</body>
</html>