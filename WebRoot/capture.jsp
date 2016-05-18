<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
<title>媒体视频</title>
<link href="css/H-ui.min.css" rel="stylesheet" type="text/css" />
<link href="css/H-ui.admin.css" rel="stylesheet" type="text/css" />
<link href="lib/Hui-iconfont/1.0.1/iconfont.css" rel="stylesheet"
	type="text/css" />

<script type="text/javascript" src="lib/jquery/1.9.1/jquery.min.js"></script>
<script type="text/javascript" src="lib/layer/1.9.3/layer.js"></script>
<script type="text/javascript" src="lib/My97DatePicker/WdatePicker.js"></script>
<script type="text/javascript"
	src="lib/datatables/1.10.0/jquery.dataTables.min.js"></script>
<script type="text/javascript" src="lib/layer/1.9.3/layer.js"></script>
<script type="text/javascript" src="js/pageKit.js"></script>
<script type="text/javascript" src="js/checkUtil.js"></script>

</head>
<script language="JavaScript">
	var params = window.location.search;
	console.log(params);
	params = params.substring(6);
	console.log(params);
	function viewData() {
		var sData = window.dialogArguments;

		window.document.getElementById("output").appendChild(sData);
		document.getElementById("sData").setAttribute("value", sData.src);
		document.getElementById("inid").setAttribute("value", params);
	}
	window.onload = viewData;
</script>
<body>
	<input id="sData1" type="hidden" name="media.picSrc" />
	<form name="mediaAddForm" action="mediaAction!add1" method="post"
		enctype="multipart/form-data" onsubmit="return checkMedia();">
		<input id="inid" type="hidden" name="media.injurycase.id" /> <input
			type="hidden" name="media.mtype" value="0" /> <input id="sData"
			type="hidden" name="media.picSrc" />
		<div class="pd-20">
			<div class="row cl mb-10">
				<div class="col-2">
					<label class="form-label text-r"> 媒体名称： </label>
				</div>
				<div class="col-4">
					<s:textfield id="number" cssClass="input-text radius size-M"
						cssStyle="width:200px;" name="media.title"></s:textfield>
				</div>
			</div>
			<div class="row cl mb-10">
				<div class="col-2">
					<label class="form-label text-r"> 媒体描述： </label>
				</div>
				<div class="col-4">
					<s:textarea name="media.descript" cssClass="input-text"
						cssStyle="width: 260px; height: 180px; float: left;"></s:textarea>
				</div>
			</div>
			<div class="row cl mb-10">
				<div class="col-2">
					<label class="form-label text-r"> 上传时间： </label>
				</div>
				<div class="col-4">
					<input type="text" name="media.uptime"
						onfocus="WdatePicker({dateFmt:'yyyy-MM-dd',readOnly:true})"
						id="logmin" class="input-text Wdate" style="width: 200px;">
				</div>
			</div>

			<div class="row cl mb-10">
				<div class="col-2">
					<label class="form-label text-r"> 截图： </label>
				</div>
				<div class="col-10">
					<div class="col-3">
						<div id="output"></div>

					</div>
				</div>
			</div>
			<div class="row cl">
				<s:token></s:token>
				<div class="col-10 col-offset-2">
					<input type="submit" class="btn btn-primary radius" value="保存并提交" />
					<button onclick="window.close();" class="btn btn-default radius"
						type="button">&nbsp;&nbsp;取消&nbsp;&nbsp;</button>
				</div>
			</div>
	</form>
</body>
</html>
