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
		<meta charset="utf-8">
		<meta name="renderer" content="webkit|ie-comp|ie-stand">
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
		<meta name="viewport"
			content="width=device-width,initial-scale=1,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no" />
		<meta http-equiv="Cache-Control" content="no-siteapp" />
		<link href="css/cba.css" rel="stylesheet" type="text/css" />
		<link href="css/H-ui.min.css" rel="stylesheet" type="text/css" />
		<link href="css/H-ui.admin.css" rel="stylesheet" type="text/css" />
		<link href="css/style.css" rel="stylesheet" type="text/css" />
		<link href="lib/Hui-iconfont/1.0.7/iconfont.css" rel="stylesheet"
			type="text/css" />
		<link rel="stylesheet"
			href="lib/zTree/v3/css/zTreeStyle/zTreeStyle.css" type="text/css">
		<script type="text/javascript" src="lib/jquery/1.9.1/jquery.min.js"></script>
		<script type="text/javascript" src="lib/layer/1.9.3/layer.js"></script>
		<script type="text/javascript" src="lib/My97DatePicker/WdatePicker.js"></script>
		<script type="text/javascript"
			src="lib/datatables/1.10.0/jquery.dataTables.min.js"></script>
		<script type="text/javascript"
			src="lib/zTree/v3/js/jquery.ztree.all-3.5.min.js"></script>
		<script type="text/javascript" src="js/H-ui.js"></script>
		<script type="text/javascript" src="js/H-ui.admin.js"></script>
		<script type="text/javascript" src="js/pageKit.js"></script>
		<script type="text/javascript" src="js/checkUtil.js"></script>
		<script type="text/javascript" src="js/commonUtil.js"></script>
		<title>串并案</title>
	</head>
	<body>
		<nav class="breadcrumb">
		<i class="Hui-iconfont">&#xe67f;</i> 首页
		<span class="c-gray en">&gt;</span>串并案
		<a class="btn btn-success radius r mr-20"
			style="line-height: 1.6em; margin-top: 3px"
			href="javascript:location.replace(location.href);" title="刷新"><i
			class="Hui-iconfont">&#xe68f;</i> </a>
		</nav>

		<div class="pd-20">
			<div class="text-c">
				<form name="injurycaseListForm" method="post"
					action="injurycaseAction!listcba" target="_self">
					<s:hidden name="type"></s:hidden>
					<table width="100%" border="0" cellspacing="0" cellpadding="0"
						style="line-height: 35px;">
						<tr height="35">
							<td width="21%" align="right" style="padding-right: 50px;">
								<s:select
									list="#{0:'选择类型',1:'案发编号',2:'案件地址',3:'案件名称',4:'录入人员姓名',5:'图片名称',6:'视频名称',7:'作案方式',8:'简要案情',9:'串并案系列名称',10:'作案工具',11:'录入单位'}"
									cssClass="input-text" name="con" listKey="key"
									listValue="value" cssStyle="width:180px"></s:select>
							</td>
							<td width="22%">
								<s:textfield name="convalue" id="convalue" cssClass="input-text"></s:textfield>
							</td>
							<td align="left" style="padding-left: 50px;">
								信息状态:
								<s:select list="#{0:'全部',1:'未办理',2:'在办理',3:'已完结'}"
									cssClass="input-text" name="queryState" listKey="key"
									listValue="value" cssStyle="width:180px"></s:select>
							</td>
							<td align="left" style="padding-left: 50px;">
							<label class="text-r">
									串并案系列名称：
								</label>
								<s:textfield name="injurycase.series"
									cssClass="input-text" id="series"
									cssStyle="width: 200px">
								</s:textfield>
								<button type="button" onclick="handleInjurycaseSeries();"
									class="btn btn-success" id="" name="" title="串并">
									串并案件
								</button>
									</td>
							
						</tr>
						<tr>
							<td colspan="2" align="right">
								案发时间：
								<input type="text" name="starttime"
									value="<s:property value="starttime"/>"
									onFocus="WdatePicker({maxDate:'#F{$dp.$D(\'endtime\')||\'%y-%M-%d\'}'})"
									id="logmin" class="input-text Wdate" style="width: 150px;">
								-
								<input type="text" name="endtime"
									value="<s:property value="endtime"/>"
									onFocus="WdatePicker({minDate:'#F{$dp.$D(\'starttime\')}',maxDate:'%y-%M-%d'})"
									id="endtime" class="input-text Wdate" style="width: 150px;">
							</td>
							<td align="left" style="padding-left: 172px;">
								<button type="submit" class="btn btn-success" id="button2"
									name="" onClick="">
									<i class="Hui-iconfont">&#xe665;</i> 查询
								</button>
								<button type="button" class="btn btn-success" id="button"
									name="">
									<i class="Hui-iconfont">&#xe66b;</i> 清空
								</button>
							</td>
						</tr>
					</table>
				</form>
			</div>
			<div class="cl pd-5 bg-1 bk-gray mt-20">
				<span class="r">共有数据：<strong><s:property
							value="totalCount" /> </strong> 条    <s:property value="injurycaseVOList.size()"></s:property></span>
			</div>
			<div class="later-play-container" style="height: 680px">

				<ul id="later-play-list" class="video-list clearfix" data-total="">
					<s:iterator value="injurycaseVOList" var="injurycase" status="status">
						<li class="video-list-item video-no-tag " data-id="66666">
							<a href="injurycaseAction!loadcba?id=<s:property value="id"/>"
								class="video-list-item-wrap" data-id="66666">
								<div class="video-cover">
									<img class="video-img video-img-lazy"
										src="<s:property value="imageCase"/>"
										style="border:1px solid rgb(100, 100, 100);display: block;" alt="暂无图片">
									<ul class="show-list">
										<li>
											<div id="videoTitle" class="show-info">
												案件名称：
												<s:property value="caseName" /></div>
											<div id="videoDesc" class="show-info">
												案发时间：
												<s:property value="startTime" />
												</div>
										</li>
									</ul>
								</div>
								<div class="video-title">
									<s:if test="isRelated==1">
										串并案系列名称：<span class="video-cate" id="series<s:property value="id"/>"><s:property
												value="series" /> </span>
									</s:if>
									<s:else>
										未串并
									</s:else>
								</div>
								<div class="video-info">
									<span class="video-nickname"><s:property
											value="userRole.username" /> 视频数：<s:property
											value="videoNumber" /> 图片数：<s:property value="imageNumher" />
									</span>
									<s:if test="handleState==0">
										<span class="video-cate">未办理</span>
									</s:if>
									<s:if test="handleState==1">
										<span class="video-cate">未办理</span>
									</s:if>
									<s:if test="handleState==2">
										<span class="video-cate">在办理</span>
									</s:if>
									<s:if test="handleState==3">
										<span class="video-cate">已完结</span>
									</s:if>
								</div> </a>
								<div style="float:right;  width: 20px; height: 20px;">
								<input style="height:16px; width:16px;" id="indexID" name="indexID"  type="checkbox"
													class="indexID" value="<s:property value="id"/>">
											</div>
						</li>
					</s:iterator>
				</ul>
			</div>
			<ul class="forminfo" style="line-height: 40px; font-size: 14px;">
				<table width="98%" border="0" align="center" cellpadding="2"
					cellspacing="1" bgcolor="#93CDF3" style="margin-top: 8px">
					<tr align="right" bgcolor="#EEF4EA">
						<td height="34" colspan="6" align="center" bgcolor="#FFFFFF">
							<a
								href="javascript:jumpInjurycaseCbaPage('injurycaseAction!listcba',1,<s:property value="con"/>,'<s:property value="convalue"/>',<s:property value="itype"/>,'<s:property value="starttime"/>','<s:property value="endtime"/>');"
								>首页</a>&nbsp;&nbsp;
							<a
								href="javascript:jumpInjurycaseCbaPage('injurycaseAction!listcba',<s:property value="page-1"/>,<s:property value="con"/>,'<s:property value="convalue"/>',<s:property value="queryState"/>,'<s:property value="starttime"/>','<s:property value="endtime"/>');"
								>上一页</a>&nbsp;&nbsp;&nbsp;
							<a
								href="javascript:jumpInjurycaseCbaPage('injurycaseAction!listcba',<s:property value="page+1"/>,<s:property value="con"/>,'<s:property value="convalue"/>',<s:property value="queryState"/>,'<s:property value="starttime"/>','<s:property value="endtime"/>');"
								>下一页</a>&nbsp;&nbsp;&nbsp;
							<a
								href="javascript:jumpInjurycaseCbaPage('injurycaseAction!listcba',<s:property value="pageCount"/>,<s:property value="con"/>,'<s:property value="convalue"/>',<s:property value="queryState"/>,'<s:property value="starttime"/>','<s:property value="endtime"/>');"
								>尾页</a>&nbsp;&nbsp;&nbsp;
							<input type='button' class="btn btn-primary radius size-S"
								onclick="javascript:jumpInjurycaseCbaPage('injurycaseAction!listcba',document.getElementById('page').value,<s:property value="con"/>,'<s:property value="convalue"/>',<s:property value="queryState"/>,'<s:property value="starttime"/>','<s:property value="endtime"/>');"
								value='转到' />
							&nbsp; 当前页：
							<input onpaste="return false"
								onkeyup="if(event.keyCode !=37 && event.keyCode != 39){if (! /^\d+$/ig.test(this.value)){this.value='';}}"
								id="page" type="text" name="page"
								value="<s:property value="page" />" size="2"
								style="width: 25px; height: 20px; line-height: 18px; BORDER-RIGHT: #cccccc 1px solid; BORDER-TOP: #cccccc 1px solid; FONT-SIZE: 13px; BORDER-LEFT: #cccccc 1px solid; COLOR: #000000; BORDER-BOTTOM: #cccccc 1px solid; FONT-FAMILY: 宋体; BACKGROUND-COLOR: #ffffff;" />
							/共
							<s:property value="pageCount" />
							页
						</td>
					</tr>
				</table>
			</ul>
		</div>
	</body>
</html>