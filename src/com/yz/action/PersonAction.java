package com.yz.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.io.FilenameUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.RequestAware;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.ActionSupport;
import com.yz.model.AnalyzeMan;
import com.yz.model.CommonClue;
import com.yz.model.ContrastMan;
import com.yz.model.DisappearMan;
import com.yz.model.GamblingCriminalMan;
import com.yz.model.GuiltSafeguardMan;
import com.yz.model.Judge;
import com.yz.model.Lawcase;
import com.yz.model.Otherperson;
import com.yz.model.Person;
import com.yz.model.Successexample;
import com.yz.model.Troubleshooting;
import com.yz.model.Unit;
import com.yz.model.UserRole;
import com.yz.service.IAnalyzeManService;
import com.yz.service.ICommonClueService;
import com.yz.service.IContrastManService;
import com.yz.service.IDisappearManService;
import com.yz.service.IGamblingCriminalManService;
import com.yz.service.IGuiltSafeguardManService;
import com.yz.service.IInjurycaseService;
import com.yz.service.IJudgeService;
import com.yz.service.ILawcaseService;
import com.yz.service.IOtherpersonService;
import com.yz.service.IPersonService;
import com.yz.service.ISocialManService;
import com.yz.service.ISuccessexampleService;
import com.yz.service.ITroubleshootingService;
import com.yz.service.IUnitService;
import com.yz.service.IUserRoleService;
import com.yz.util.ConvertUtil;
import com.yz.util.DateTimeKit;
import com.yz.vo.AjaxMsgVO;
import com.yz.vo.SocialManForm;
import com.yz.vo.UnitVO;

@Component("personAction")
@Scope("prototype")
public class PersonAction extends ActionSupport implements RequestAware,
		SessionAware, ServletResponseAware, ServletRequestAware {

	private static final long serialVersionUID = 1L;
	Map<String, Object> request;
	Map<String, Object> session;
	private javax.servlet.http.HttpServletResponse response;
	private javax.servlet.http.HttpServletRequest req;

	// 分页显示
	private String[] arg = new String[2];
	private int page;
	private final int size = 10;
	private int pageCount;
	private int totalCount;

	// 条件
	private int id;
	private int pid;// 按用户id

	private int lawid;// 涉及案件
	private int troubid;// 疑难问题
	private int jid;// 研判情况
	private int otherid;// 同案人员，关系人员
	private int con;
	private String convalue;
	private int status;// 按状态
	private int type;// 人员类型
	private int otype;// 其他人员类型 1：关系人员，2：同案人员
	private int jtype;// 发起类型 1:研判信息 2：部门查证 3：上报情况
	private int queryState;// 办理状态
	private String starttime;
	private String endtime;

	// 页面信息
	private String pageTileName;// 页面标题名称
	private List<String> infoExtractions;// 页面显示被选中 信息提取情况

	// 批量删除
	private String checkedIDs;

	// service层对象
	private IUnitService unitService;

	private IPersonService personService;
	private IGamblingCriminalManService gamblingCriminalManService;
	private IGuiltSafeguardManService guiltSafeguardManService;
	private IDisappearManService disappearmanService;
	private IAnalyzeManService analyzeManService;
	private IContrastManService contrastManService;
	private ICommonClueService commonClueService;

	private ILawcaseService lawcaseService;
	private ITroubleshootingService troubleshootingService;
	private IJudgeService judgeService;
	private IOtherpersonService otherpersonService;
	private IInjurycaseService injurycaseService;

	private ISuccessexampleService successexampleService;

	private IUserRoleService userRoleService;

	// 单个表对象
	private Person person;
	private GamblingCriminalMan gamblingCriminalMan;// 1:赌博人员，2:涉恶人员，3:涉黄人员，4:食药环人员，5:涉毒人员，6:留置盘问，7:侵财人员，8:刑事传唤
	private GuiltSafeguardMan guiltSafeguardMan;// 9:负案在逃人员,10:维稳人员
	private DisappearMan disappearman;// 11:失踪人员
	private AnalyzeMan analyzeMan;// 12:侵财分析人员
	private ContrastMan contrastMan;// 13:技术比中人员
	private CommonClue commonClue;// 14:普通线索人员(普通线索)

	private Lawcase lawcase;
	private Troubleshooting troubleshooting;
	private Judge judge;
	private Otherperson otherperson;

	private Successexample successexample;

	private UnitVO unitVO;
	private Unit unit;

	// list表对象
	private List<Person> persons;
	private List<UnitVO> unitVOs;
	private List<Unit> units;
	private List<Judge> judges;
	private List<Otherperson> otherpersons;
	private List<Otherperson> gxrs;// 关系人员
	private List<Otherperson> tars;// 同案人员
	private List<Otherperson> xyrs;// 嫌疑人员
	// 权限
	private int ulimit;

	// 部门json
	private String jsonUnits;
	@Resource
	private ISocialManService socialManService;

	// 测试file
	private File file;
	private String fileContentType;
	private String fileFileName;

	/**
	 * 人员管理
	 */
	public String list() throws Exception {

		// 登陆验证
		UserRole userRoleo = (UserRole) session.get("userRoleo");
		if (userRoleo == null) {
			return "opsessiongo";
		}

		UserRole userRole = userRoleService.loadById(userRoleo.getId());

		if (convalue != null && !convalue.equals("")) {
			convalue = URLDecoder.decode(convalue, "utf-8");
		}
		if (starttime != null && !starttime.equals("")) {
			starttime = URLDecoder.decode(starttime, "utf-8");
		}
		if (endtime != null && !endtime.equals("")) {
			endtime = URLDecoder.decode(endtime, "utf-8");
		}
		if (page < 1) {
			page = 1;
		}

		pageTileName = selectTileName(type);

		// 总记录数
		totalCount = personService.getTotalCount(con, convalue, userRole, type,
				queryState, starttime, endtime);
		// 总页数
		pageCount = personService.getPageCount(totalCount, size);
		if (page > pageCount && pageCount != 0) {
			page = pageCount;
		}
		// 所有当前页记录对象
		persons = personService.queryList(con, convalue, userRole, page, size,
				type, queryState, starttime, endtime);

		return "list";
	}

	// 选择页面名称
	private String selectTileName(int type) {
		String pageName = "人员信息";
		switch (type) {
		case 0:
			pageName = "人员";
			break;
		case 1:
			pageName = "赌博人员";
			break;
		case 2:
			pageName = "涉恶人员";
			break;
		case 3:
			pageName = "涉黄人员";
			break;
		case 4:
			pageName = "食药环人员";
			break;
		case 5:
			pageName = "涉毒人员";
			break;
		case 6:
			pageName = "留置盘问人员";
			break;
		case 7:
			pageName = "侵财人员人员";
			break;
		case 8:
			pageName = "刑事传唤人员";
			break;
		case 9:
			pageName = "负案在逃人员";
			break;
		case 10:
			pageName = "维稳人员";
			break;
		case 11:
			pageName = "失踪人员";
			break;
		case 12:
			pageName = "侵财人员分析";
			break;
		case 13:
			pageName = "技术比中人员";
			break;
		case 14:
			pageName = "普通线索";
			break;
		case 15:
			pageName = "社会人员";
			break;
		default:
			break;
		}
		return pageName;
	}

	/**
	 * 跳转到添加页面
	 * 
	 * @return
	 */
	public String goToAdd() {

		UserRole userRoleo = (UserRole) session.get("userRoleo");
		if (userRoleo == null) {
			return "opsessiongo";
		}
		pageTileName = selectTileName(type);
		switch (type) {
		case 0:
		case 1:
			// pageName = "赌博人员";
		case 2:
			// pageName = "涉恶人员";
		case 3:
			// pageName = "涉黄人员";
		case 4:
			// pageName = "食药环人员";
		case 5:
			// pageName = "涉毒人员";
		case 6:
			// pageName = "留置盘问";
		case 7:
			// pageName = "侵财人员";
		case 8:
			// pageName = "刑事传唤";
			return "gamblingCriminalMan_add";
		case 9:
			// pageName = "负罪在逃";
		case 10:
			// pageName = "维稳人员";
			return "guiltSafeguardMan_add";
		case 11:
			// pageName = "失踪人员";
			return "disappearman_add";
		case 12:
			// pageName = "侵财人员分析";
			return "analyzeMan_add";
		case 13:
			// pageName = "技术比中人员";
			return "contrastMan_add";
		case 14:
			// pageName = "普通线索";
			return "commonClue_add";
		case 15:
			// pageName = "社会人员"
			return "socialMan_add";
		default:
			return "add";
		}

	}

	/**
	 * 添加
	 * 
	 * @return
	 * @throws Exception
	 */

	public String add() throws Exception {

		UserRole userRoleo = (UserRole) session.get("userRoleo");
		if (userRoleo == null) {
			return "opsessiongo_child";
		}
		type = person.getType();
		// 分类添加人员信息
		switch (type) {
		case 0:
		case 1:
			// pageName = "赌博人员";
		case 2:
			// pageName = "涉恶人员";
		case 3:
			// pageName = "涉黄人员";
		case 4:
			// pageName = "食药环人员";
		case 5:
			// pageName = "涉毒人员";
		case 6:
			// pageName = "留置盘问";
		case 7:
			// pageName = "侵财人员";
		case 8:
			// pageName = "刑事传唤";
			if (gamblingCriminalMan == null) {
				gamblingCriminalMan = new GamblingCriminalMan();
			}
			gamblingCriminalManService.add(gamblingCriminalMan);
			person.setGamblingCriminalMan(gamblingCriminalMan);
			break;
		case 9:
			// pageName = "负罪在逃";
		case 10:
			// pageName = "维稳人员";
			if (guiltSafeguardMan == null) {
				guiltSafeguardMan = new GuiltSafeguardMan();
			}
			if (picture1 != null && picture1FileName != null
					&& !picture1FileName.replace(" ", "").equals("")) {
				String imageName = DateTimeKit.getDateRandom()
						+ picture1FileName.substring(picture1FileName
								.indexOf("."));
				this.upload("/guiltSafeguardMan", imageName, picture1);
				guiltSafeguardMan.setCriminalRecordPhoto1("guiltSafeguardMan"
						+ "/" + imageName);
			}
			if (picture2 != null && picture2FileName != null
					&& !picture2FileName.replace(" ", "").equals("")) {
				String imageName = DateTimeKit.getDateRandom()
						+ picture2FileName.substring(picture2FileName
								.indexOf("."));
				this.upload("/guiltSafeguardMan", imageName, picture2);
				guiltSafeguardMan.setCriminalRecordPhoto2("guiltSafeguardMan"
						+ "/" + imageName);
			}
			if (picture3 != null && picture3FileName != null
					&& !picture3FileName.replace(" ", "").equals("")) {
				String imageName = DateTimeKit.getDateRandom()
						+ picture3FileName.substring(picture3FileName
								.indexOf("."));
				this.upload("/guiltSafeguardMan", imageName, picture3);
				guiltSafeguardMan.setCriminalRecordPhoto3("guiltSafeguardMan"
						+ "/" + imageName);
			}
			guiltSafeguardManService.add(guiltSafeguardMan);
			person.setGuiltSafeguardMan(guiltSafeguardMan);
			break;
		case 11:
			// pageName = "失踪人员分析";
			if (disappearman == null) {
				disappearman = new DisappearMan();
			}
			if (picture1 != null && picture1FileName != null
					&& !picture1FileName.replace(" ", "").equals("")) {
				String imageName = DateTimeKit.getDateRandom()
						+ picture1FileName.substring(picture1FileName
								.indexOf("."));
				this.upload("/disappearman", imageName, picture1);
				disappearman.setPhoto1("disappearman" + "/" + imageName);
			}
			if (picture2 != null && picture2FileName != null
					&& !picture2FileName.replace(" ", "").equals("")) {
				String imageName = DateTimeKit.getDateRandom()
						+ picture2FileName.substring(picture2FileName
								.indexOf("."));
				this.upload("/disappearman", imageName, picture2);
				disappearman.setPhoto2("disappearman" + "/" + imageName);
			}
			if (picture3 != null && picture3FileName != null
					&& !picture3FileName.replace(" ", "").equals("")) {
				String imageName = DateTimeKit.getDateRandom()
						+ picture3FileName.substring(picture3FileName
								.indexOf("."));
				this.upload("/disappearman", imageName, picture3);
				disappearman.setPhoto3("disappearman" + "/" + imageName);
			}
			disappearmanService.add(disappearman);
			person.setDisappearMan(disappearman);
			break;
		case 12:
			// pageName = "侵财人员分析";
			if (analyzeMan == null) {
				analyzeMan = new AnalyzeMan();
			}
			analyzeManService.add(analyzeMan);
			person.setAnalyzeMan(analyzeMan);
			break;
		case 13:
			// pageName = "技术比中人员";
			if (contrastMan == null) {
				contrastMan = new ContrastMan();
			}
			if (picture1 != null && picture1FileName != null
					&& !picture1FileName.replace(" ", "").equals("")) {
				String imageName = DateTimeKit.getDateRandom()
						+ picture1FileName.substring(picture1FileName
								.indexOf("."));
				this.upload("/contrastMan", imageName, picture1);
				contrastMan.setRegisterAddressPhoto("contrastMan" + "/"
						+ imageName);
			}
			if (picture2 != null && picture2FileName != null
					&& !picture2FileName.replace(" ", "").equals("")) {
				String imageName = DateTimeKit.getDateRandom()
						+ picture2FileName.substring(picture2FileName
								.indexOf("."));
				this.upload("/contrastMan", imageName, picture2);
				contrastMan.setCriminalRecordPhoto("contrastMan" + "/"
						+ imageName);
			}
			contrastManService.add(contrastMan);
			person.setContrastMan(contrastMan);
			break;
		case 14:
			// pageName = "普通线索";
			if (commonClue == null) {
				commonClue = new CommonClue();
			}
			if (picture1 != null && picture1FileName != null
					&& !picture1FileName.replace(" ", "").equals("")) {
				String imageName = DateTimeKit.getDateRandom()
						+ picture1FileName.substring(picture1FileName
								.indexOf("."));
				this.upload("/commonClue", imageName, picture1);
				commonClue.setRegisterAddressPhoto("commonClue" + "/"
						+ imageName);
			}
			if (picture2 != null && picture2FileName != null
					&& !picture2FileName.replace(" ", "").equals("")) {
				String imageName = DateTimeKit.getDateRandom()
						+ picture2FileName.substring(picture2FileName
								.indexOf("."));
				this.upload("/commonClue", imageName, picture2);
				commonClue.setCriminalRecordPhoto("commonClue" + "/"
						+ imageName);
			}
			commonClueService.add(commonClue);
			person.setCommonClue(commonClue);
			break;
		default:
			break;
		}

		if (picture != null && pictureFileName != null
				&& !pictureFileName.replace(" ", "").equals("")) {
			String imageName = DateTimeKit.getDateRandom()
					+ pictureFileName.substring(pictureFileName.indexOf("."));
			this.upload("/person", imageName, picture);
			person.setPhotoImg("person" + "/" + imageName);
		}

		UserRole userRole = userRoleService.loadById(userRoleo.getId());
		person.setUserRole(userRole);// 设置录入人员
		person.setJoinDate(DateTimeKit.getLocalDate());// 设置录入时间
		person.setHandleState(1);// 初始化处理状态
		personService.add(person);

		// 添加当前用户id到部门pids
		if (userRole.getUnit() != null) {
			int uid = userRole.getUnit().getId();
			Unit un = unitService.loadById(uid);

			if (un.getPids() != null && un.getPids() != "") {
				un.setPids(handleIDs(un.getPids(), person.getId() + ""));
			} else {
				un.setPids(person.getId() + ",");
			}
			unitService.update(un);
		}

		arg[0] = "personAction!list?type=" + person.getType();
		arg[1] = "人员管理";
		return "success_child";
	}

	// 处理ids
	private String handleIDs(String objIDs, String objID) {
		Set<String> ids = new HashSet<String>();
		String newIDs = "";
		String[] arrayIDs = objIDs.split(",");
		for (int i = 0; i < arrayIDs.length; i++) {
			ids.add(arrayIDs[i]);
		}
		ids.add(objID);

		for (String id : ids) {
			newIDs = newIDs + id + ",";
		}
		System.out.println(newIDs);
		return newIDs;
	}

	// 上传照片
	private File picture;
	private String pictureContentType;
	private String pictureFileName;

	// 文件上传
	public void upload(String fileName, String imageName, File picture)
			throws Exception {
		File saved = new File(ServletActionContext.getServletContext()
				.getRealPath(fileName), imageName);
		InputStream ins = null;
		OutputStream ous = null;
		try {
			saved.getParentFile().mkdirs();
			ins = new FileInputStream(picture);
			ous = new FileOutputStream(saved);
			byte[] b = new byte[1024];
			int len = 0;
			while ((len = ins.read(b)) != -1) {
				ous.write(b, 0, len);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ous != null)
				ous.close();
			if (ins != null)
				ins.close();
		}
	}

	/**
	 * 9:负案在逃人员,10:维稳人员 上传前科照片
	 */
	private File picture1;
	private String picture1ContentType;
	private String picture1FileName;
	private File picture2;
	private String picture2ContentType;
	private String picture2FileName;
	private File picture3;
	private String picture3ContentType;
	private String picture3FileName;
	private SocialManForm socialManForm;

	/**
	 * 删除一
	 * 
	 * @return
	 */
	public String delete() throws Exception {
		UserRole userRoleo = (UserRole) session.get("userRoleo");
		if (userRoleo == null) {
			return "opsessiongo";
		}
		person = personService.loadById(id);

		type = person.getType();
		// 分类添加人员信息
		switch (type) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
			id = person.getGamblingCriminalMan().getId();
			break;
		case 9:
		case 10:
			id = person.getGuiltSafeguardMan().getId();
			guiltSafeguardMan = guiltSafeguardManService.loadById(id);
			if (guiltSafeguardMan != null) {
				if (guiltSafeguardMan.getCriminalRecordPhoto1() != null
						&& !guiltSafeguardMan.getCriminalRecordPhoto1()
								.replace(" ", "").equals("")) {
					File photofile = new File(ServletActionContext
							.getServletContext().getRealPath("/")
							+ guiltSafeguardMan.getCriminalRecordPhoto1());
					photofile.delete();
				}
				if (guiltSafeguardMan.getCriminalRecordPhoto2() != null
						&& !guiltSafeguardMan.getCriminalRecordPhoto2()
								.replace(" ", "").equals("")) {
					File photofile = new File(ServletActionContext
							.getServletContext().getRealPath("/")
							+ guiltSafeguardMan.getCriminalRecordPhoto2());
					photofile.delete();
				}
				if (guiltSafeguardMan.getCriminalRecordPhoto3() != null
						&& !guiltSafeguardMan.getCriminalRecordPhoto3()
								.replace(" ", "").equals("")) {
					File photofile = new File(ServletActionContext
							.getServletContext().getRealPath("/")
							+ guiltSafeguardMan.getCriminalRecordPhoto3());
					photofile.delete();
				}
				// 同时删除所有同安人员，关系人员的照片
				otherpersons = otherpersonService.getOtherpersons();
				if (otherpersons != null && otherpersons.size() > 0) {
					for (Otherperson otherperson : otherpersons) {
						if (otherperson.getFrontPhoto() != null
								&& !otherperson.getFrontPhoto()
										.replace(" ", "").equals("")) {
							File photofile = new File(ServletActionContext
									.getServletContext().getRealPath("/")
									+ otherperson.getFrontPhoto());
							photofile.delete();
						}
						if (otherperson.getLeftPhoto() != null
								&& !otherperson.getLeftPhoto().replace(" ", "")
										.equals("")) {
							File photofile = new File(ServletActionContext
									.getServletContext().getRealPath("/")
									+ otherperson.getLeftPhoto());
							photofile.delete();
						}
						if (otherperson.getRightPhoto() != null
								&& !otherperson.getRightPhoto()
										.replace(" ", "").equals("")) {
							File photofile = new File(ServletActionContext
									.getServletContext().getRealPath("/")
									+ otherperson.getRightPhoto());
							photofile.delete();
						}
					}
				}
			}
			break;
		case 11:
			id = person.getDisappearMan().getId();
			disappearman = disappearmanService.loadById(id);
			if (disappearman != null) {
				if (disappearman.getPhoto1() != null
						&& !disappearman.getPhoto1().replace(" ", "")
								.equals("")) {
					File photofile = new File(ServletActionContext
							.getServletContext().getRealPath("/")
							+ disappearman.getPhoto1());
					photofile.delete();
				}
				if (disappearman.getPhoto2() != null
						&& !disappearman.getPhoto2().replace(" ", "")
								.equals("")) {
					File photofile = new File(ServletActionContext
							.getServletContext().getRealPath("/")
							+ disappearman.getPhoto2());
					photofile.delete();
				}
				if (disappearman.getPhoto3() != null
						&& !disappearman.getPhoto3().replace(" ", "")
								.equals("")) {
					File photofile = new File(ServletActionContext
							.getServletContext().getRealPath("/")
							+ disappearman.getPhoto3());
					photofile.delete();
				}
			}
			break;
		case 12:
			break;
		case 13:
			id = person.getContrastMan().getId();
			contrastMan = contrastManService.loadById(id);
			if (contrastMan != null) {
				if (contrastMan.getRegisterAddressPhoto() != null
						&& !contrastMan.getRegisterAddressPhoto().replace(" ",
								"").equals("")) {
					File photofile = new File(ServletActionContext
							.getServletContext().getRealPath("/")
							+ contrastMan.getRegisterAddressPhoto());
					photofile.delete();
				}
				if (contrastMan.getCriminalRecordPhoto() != null
						&& !contrastMan.getCriminalRecordPhoto().replace(" ",
								"").equals("")) {
					File photofile = new File(ServletActionContext
							.getServletContext().getRealPath("/")
							+ contrastMan.getCriminalRecordPhoto());
					photofile.delete();
				}
			}
			break;
		case 14:
			id = person.getCommonClue().getId();
			commonClue = commonClueService.loadById(id);
			if (commonClue != null) {
				if (commonClue.getRegisterAddressPhoto() != null
						&& !commonClue.getRegisterAddressPhoto().replace(" ",
								"").equals("")) {
					File photofile = new File(ServletActionContext
							.getServletContext().getRealPath("/")
							+ commonClue.getRegisterAddressPhoto());
					photofile.delete();
				}
				if (commonClue.getCriminalRecordPhoto() != null
						&& !commonClue.getCriminalRecordPhoto()
								.replace(" ", "").equals("")) {
					File photofile = new File(ServletActionContext
							.getServletContext().getRealPath("/")
							+ commonClue.getCriminalRecordPhoto());
					photofile.delete();
				}
			}
			break;
		default:
			break;
		}

		// 删除照片
		File photofile = new File(ServletActionContext.getServletContext()
				.getRealPath("/")
				+ person.getPhotoImg());
		photofile.delete();
		personService.delete(person);

		arg[0] = "personAction!list?type=" + type;
		arg[1] = "人员管理";
		return SUCCESS;
	}

	/**
	 * 删除二(批量删除)
	 * 
	 * @return
	 */
	public String deletePersons() throws Exception {

		int[] ids = ConvertUtil.StringtoInt(checkedIDs);
		for (int i = 0; i < ids.length; i++) {
			person = personService.loadById(ids[i]);

			type = person.getType();
			// 分类添加人员信息
			switch (type) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
				break;
			case 9:
			case 10:
				id = person.getGuiltSafeguardMan().getId();
				guiltSafeguardMan = guiltSafeguardManService.loadById(id);
				if (guiltSafeguardMan != null) {
					if (guiltSafeguardMan.getCriminalRecordPhoto1() != null
							&& !guiltSafeguardMan.getCriminalRecordPhoto1()
									.replace(" ", "").equals("")) {
						File photofile = new File(ServletActionContext
								.getServletContext().getRealPath("/")
								+ guiltSafeguardMan.getCriminalRecordPhoto1());
						photofile.delete();
					}
					if (guiltSafeguardMan.getCriminalRecordPhoto2() != null
							&& !guiltSafeguardMan.getCriminalRecordPhoto2()
									.replace(" ", "").equals("")) {
						File photofile = new File(ServletActionContext
								.getServletContext().getRealPath("/")
								+ guiltSafeguardMan.getCriminalRecordPhoto2());
						photofile.delete();
					}
					if (guiltSafeguardMan.getCriminalRecordPhoto3() != null
							&& !guiltSafeguardMan.getCriminalRecordPhoto3()
									.replace(" ", "").equals("")) {
						File photofile = new File(ServletActionContext
								.getServletContext().getRealPath("/")
								+ guiltSafeguardMan.getCriminalRecordPhoto3());
						photofile.delete();
					}
				}
				break;
			case 11:
				id = person.getDisappearMan().getId();
				disappearman = disappearmanService.loadById(id);
				if (disappearman != null) {
					if (disappearman.getPhoto1() != null
							&& !disappearman.getPhoto1().replace(" ", "")
									.equals("")) {
						File photofile = new File(ServletActionContext
								.getServletContext().getRealPath("/")
								+ disappearman.getPhoto1());
						photofile.delete();
					}
					if (disappearman.getPhoto2() != null
							&& !disappearman.getPhoto2().replace(" ", "")
									.equals("")) {
						File photofile = new File(ServletActionContext
								.getServletContext().getRealPath("/")
								+ disappearman.getPhoto2());
						photofile.delete();
					}
					if (disappearman.getPhoto3() != null
							&& !disappearman.getPhoto3().replace(" ", "")
									.equals("")) {
						File photofile = new File(ServletActionContext
								.getServletContext().getRealPath("/")
								+ disappearman.getPhoto3());
						photofile.delete();
					}
				}
				break;
			case 12:
				break;
			case 13:
				id = person.getContrastMan().getId();
				contrastMan = contrastManService.loadById(id);
				if (contrastMan != null) {
					if (contrastMan.getRegisterAddressPhoto() != null
							&& !contrastMan.getRegisterAddressPhoto().replace(
									" ", "").equals("")) {
						File photofile = new File(ServletActionContext
								.getServletContext().getRealPath("/")
								+ contrastMan.getRegisterAddressPhoto());
						photofile.delete();
					}
					if (contrastMan.getCriminalRecordPhoto() != null
							&& !contrastMan.getCriminalRecordPhoto().replace(
									" ", "").equals("")) {
						File photofile = new File(ServletActionContext
								.getServletContext().getRealPath("/")
								+ contrastMan.getCriminalRecordPhoto());
						photofile.delete();
					}
				}
				break;
			case 14:
				id = person.getCommonClue().getId();
				commonClue = commonClueService.loadById(id);
				if (commonClue != null) {
					if (commonClue.getRegisterAddressPhoto() != null
							&& !commonClue.getRegisterAddressPhoto().replace(
									" ", "").equals("")) {
						File photofile = new File(ServletActionContext
								.getServletContext().getRealPath("/")
								+ commonClue.getRegisterAddressPhoto());
						photofile.delete();
					}
					if (commonClue.getCriminalRecordPhoto() != null
							&& !commonClue.getCriminalRecordPhoto().replace(
									" ", "").equals("")) {
						File photofile = new File(ServletActionContext
								.getServletContext().getRealPath("/")
								+ commonClue.getCriminalRecordPhoto());
						photofile.delete();
					}
				}
				break;
			default:
				break;
			}

			File photofile = new File(ServletActionContext.getServletContext()
					.getRealPath("/")
					+ person.getPhotoImg());
			photofile.delete();
			personService.delete(person);
		}
		AjaxMsgVO msgVO = new AjaxMsgVO();
		msgVO.setMessage("批量删除成功.");
		JSONObject jsonObj = JSONObject.fromObject(msgVO);
		PrintWriter out;
		try {
			response.setContentType("text/html;charset=UTF-8");
			out = response.getWriter();
			out.print(jsonObj.toString());
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 跳转到修改页面
	 * 
	 * @return
	 */
	public String load() throws Exception {
		UserRole userRoleo = (UserRole) session.get("userRoleo");
		if (userRoleo == null) {
			return "opsessiongo";
		}
		pageTileName = selectTileName(type);

		person = personService.queryPersonById(id);// 当前修改人员的id

		type = person.getType();
		// 分类添加人员信息
		switch (type) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
			gamblingCriminalMan = person.getGamblingCriminalMan();
			if (gamblingCriminalMan != null) {
				handleInfoExtractionMsg(gamblingCriminalMan.getInfoExtraction());
			}
			return "gamblingCriminalMan_load";
		case 9:
		case 10:
			guiltSafeguardMan = person.getGuiltSafeguardMan();
			gxrs = otherpersonService.getOtherpersonByOtype(1, id);// 关系人
			tars = otherpersonService.getOtherpersonByOtype(2, id);// 同案人
			return "guiltSafeguardMan_load";
		case 11:
			disappearman = person.getDisappearMan();
			return "disappearman_load";
		case 12:
			analyzeMan = person.getAnalyzeMan();
			return "analyzeMan_load";
		case 13:
			contrastMan = person.getContrastMan();
			return "contrastMan_load";
		case 14:
			commonClue = person.getCommonClue();
			return "commonClue_load";

		default:
			return "load";
		}

	}

	// 页面显示被选中 信息提前情况 {'提取手机信息','提取银行卡信息','提取DNA','提取指纹','提取鞋印'}显示格式
	private void handleInfoExtractionMsg(String infoExtraction) {
		infoExtractions = new ArrayList<String>();
		if (infoExtraction != null && infoExtraction.length() > 0
				&& infoExtraction.contains(",")) {
			String[] infoExtractionpres = infoExtraction.split(",");
			for (int i = 0; i < infoExtractionpres.length; i++) {
				infoExtractions.add(infoExtractionpres[i].replace(" ", ""));
			}
		}
	}

	/**
	 * 修改
	 * 
	 * @return
	 */
	public String update() throws Exception {
		UserRole userRoleo = (UserRole) session.get("userRoleo");
		if (userRoleo == null) {
			return "opsessiongo_child";
		}
		type = person.getType();
		// 分类添加人员信息
		switch (type) {
		case 0:
		case 1:
			// pageName = "赌博人员";
		case 2:
			// pageName = "涉恶人员";
		case 3:
			// pageName = "涉黄人员";
		case 4:
			// pageName = "食药环人员";
		case 5:
			// pageName = "涉毒人员";
		case 6:
			// pageName = "留置盘问";
		case 7:
			// pageName = "侵财人员";
		case 8:
			// pageName = "刑事传唤";
			gamblingCriminalManService.update(gamblingCriminalMan);
			person.setGamblingCriminalMan(gamblingCriminalMan);
			break;
		case 9:
			// pageName = "负罪在逃";
		case 10:
			// pageName = "维稳人员";
			if (picture1 != null && picture1FileName != null
					&& !picture1FileName.replace(" ", "").equals("")) {
				String imageName = DateTimeKit.getDateRandom()
						+ picture1FileName.substring(picture1FileName
								.indexOf("."));
				this.upload("/guiltSafeguardMan", imageName, picture1);
				File photofile = new File(ServletActionContext
						.getServletContext().getRealPath("/")
						+ guiltSafeguardMan.getCriminalRecordPhoto1());
				photofile.delete();
				guiltSafeguardMan.setCriminalRecordPhoto1("guiltSafeguardMan"
						+ "/" + imageName);
			}
			if (picture2 != null && picture2FileName != null
					&& !picture2FileName.replace(" ", "").equals("")) {
				String imageName = DateTimeKit.getDateRandom()
						+ picture2FileName.substring(picture2FileName
								.indexOf("."));
				this.upload("/guiltSafeguardMan", imageName, picture2);
				File photofile = new File(ServletActionContext
						.getServletContext().getRealPath("/")
						+ guiltSafeguardMan.getCriminalRecordPhoto2());
				photofile.delete();
				guiltSafeguardMan.setCriminalRecordPhoto2("guiltSafeguardMan"
						+ "/" + imageName);
			}
			if (picture3 != null && picture3FileName != null
					&& !picture3FileName.replace(" ", "").equals("")) {
				String imageName = DateTimeKit.getDateRandom()
						+ picture3FileName.substring(picture3FileName
								.indexOf("."));
				this.upload("/guiltSafeguardMan", imageName, picture3);
				File photofile = new File(ServletActionContext
						.getServletContext().getRealPath("/")
						+ guiltSafeguardMan.getCriminalRecordPhoto3());
				photofile.delete();
				guiltSafeguardMan.setCriminalRecordPhoto3("guiltSafeguardMan"
						+ "/" + imageName);
			}
			guiltSafeguardManService.update(guiltSafeguardMan);
			person.setGuiltSafeguardMan(guiltSafeguardMan);
			break;
		case 11:
			// pageName = "失踪人员";
			if (picture1 != null && picture1FileName != null
					&& !picture1FileName.replace(" ", "").equals("")) {
				String imageName = DateTimeKit.getDateRandom()
						+ picture1FileName.substring(picture1FileName
								.indexOf("."));
				this.upload("/disappearman", imageName, picture1);
				File photofile = new File(ServletActionContext
						.getServletContext().getRealPath("/")
						+ disappearman.getPhoto1());
				photofile.delete();
				disappearman.setPhoto1("disappearman" + "/" + imageName);
			}
			if (picture2 != null && picture2FileName != null
					&& !picture2FileName.replace(" ", "").equals("")) {
				String imageName = DateTimeKit.getDateRandom()
						+ picture2FileName.substring(picture2FileName
								.indexOf("."));
				this.upload("/disappearman", imageName, picture2);
				File photofile = new File(ServletActionContext
						.getServletContext().getRealPath("/")
						+ disappearman.getPhoto2());
				photofile.delete();
				disappearman.setPhoto2("disappearman" + "/" + imageName);
			}
			if (picture3 != null && disappearman != null
					&& !picture3FileName.replace(" ", "").equals("")) {
				String imageName = DateTimeKit.getDateRandom()
						+ picture3FileName.substring(picture3FileName
								.indexOf("."));
				this.upload("/disappearman", imageName, picture3);
				File photofile = new File(ServletActionContext
						.getServletContext().getRealPath("/")
						+ disappearman.getPhoto3());
				photofile.delete();
				disappearman.setPhoto3("disappearman" + "/" + imageName);
			}
			disappearmanService.update(disappearman);
			person.setDisappearMan(disappearman);
			break;
		case 12:
			// pageName = "侵财人员分析";
			analyzeManService.update(analyzeMan);
			person.setAnalyzeMan(analyzeMan);
			break;
		case 13:
			// pageName = "技术比中人员";
			if (picture1 != null && picture1FileName != null
					&& !picture1FileName.replace(" ", "").equals("")) {
				String imageName = DateTimeKit.getDateRandom()
						+ picture1FileName.substring(picture1FileName
								.indexOf("."));
				this.upload("/contrastMan", imageName, picture1);
				File photofile = new File(ServletActionContext
						.getServletContext().getRealPath("/")
						+ contrastMan.getRegisterAddressPhoto());
				photofile.delete();
				contrastMan.setRegisterAddressPhoto("contrastMan" + "/"
						+ imageName);
			}
			if (picture2 != null && picture2FileName != null
					&& !picture2FileName.replace(" ", "").equals("")) {
				String imageName = DateTimeKit.getDateRandom()
						+ picture2FileName.substring(picture2FileName
								.indexOf("."));
				this.upload("/contrastMan", imageName, picture2);
				File photofile = new File(ServletActionContext
						.getServletContext().getRealPath("/")
						+ contrastMan.getCriminalRecordPhoto());
				photofile.delete();
				contrastMan.setCriminalRecordPhoto("contrastMan" + "/"
						+ imageName);
			}
			contrastManService.update(contrastMan);
			person.setContrastMan(contrastMan);
			break;
		case 14:
			// pageName = "普通线索";
			if (picture1 != null && picture1FileName != null
					&& !picture1FileName.replace(" ", "").equals("")) {
				String imageName = DateTimeKit.getDateRandom()
						+ picture1FileName.substring(picture1FileName
								.indexOf("."));
				this.upload("/commonClue", imageName, picture1);
				File photofile = new File(ServletActionContext
						.getServletContext().getRealPath("/")
						+ commonClue.getRegisterAddressPhoto());
				photofile.delete();
				commonClue.setRegisterAddressPhoto("commonClue" + "/"
						+ imageName);
			}
			if (picture2 != null && picture2FileName != null
					&& !picture2FileName.replace(" ", "").equals("")) {
				String imageName = DateTimeKit.getDateRandom()
						+ picture2FileName.substring(picture2FileName
								.indexOf("."));
				this.upload("/commonClue", imageName, picture2);
				File photofile = new File(ServletActionContext
						.getServletContext().getRealPath("/")
						+ commonClue.getCriminalRecordPhoto());
				photofile.delete();
				commonClue.setCriminalRecordPhoto("commonClue" + "/"
						+ imageName);
			}
			commonClueService.update(commonClue);
			person.setCommonClue(commonClue);
			break;
		default:
			break;
		}

		if (picture != null && pictureFileName != null
				&& !pictureFileName.replace(" ", "").equals("")) {
			String imageName = DateTimeKit.getDateRandom()
					+ pictureFileName.substring(pictureFileName.indexOf("."));
			this.upload("/person", imageName, picture);
			File photofile = new File(ServletActionContext.getServletContext()
					.getRealPath("/")
					+ person.getPhotoImg());
			photofile.delete();
			person.setPhotoImg("person" + "/" + imageName);
		}

		if (person.getEndSituation() != null && person.getEndSituation() != "") {
			person.setHandleState(3);// 完结
		}

		if (person.getUserRole() == null) {
			UserRole userRole = userRoleService.loadById(userRoleo.getId());
			person.setUserRole(userRole);// 设置录入人员
		}

		personService.update(person);

		arg[0] = "personAction!list?type=" + person.getType();
		arg[1] = "人员管理";
		return "success_child";
	}

	/**
	 * 查看信息
	 * 
	 * @return
	 */
	public String view() {
		UserRole userRoleo = (UserRole) session.get("userRoleo");
		if (userRoleo == null) {
			return "opsessiongo";
		}
		person = personService.loadById(id);
		return "view";
	}

	// get、set-------------------------------------------

	// 获得HttpServletResponse对象
	public void setServletResponse(HttpServletResponse response) {
		this.response = response;
	}

	public void setServletRequest(HttpServletRequest req) {
		this.req = req;
	}

	public Map<String, Object> getRequest() {
		return request;
	}

	public void setRequest(Map<String, Object> request) {
		this.request = request;
	}

	public Map<String, Object> getSession() {
		return session;
	}

	public void setSession(Map<String, Object> session) {
		this.session = session;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getCon() {
		return con;
	}

	public void setCon(int con) {
		this.con = con;
	}

	public String getConvalue() {
		return convalue;
	}

	public void setConvalue(String convalue) {
		this.convalue = convalue;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public String[] getArg() {
		return arg;
	}

	public void setArg(String[] arg) {
		this.arg = arg;
	}

	public IPersonService getPersonService() {
		return personService;
	}

	@Resource
	public void setPersonService(IPersonService personService) {
		this.personService = personService;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public List<Person> getPersons() {
		return persons;
	}

	public void setPersons(List<Person> persons) {
		this.persons = persons;
	}

	public String getCheckedIDs() {
		return checkedIDs;
	}

	public void setCheckedIDs(String checkedIDs) {
		this.checkedIDs = checkedIDs;
	}

	public int getUlimit() {
		return ulimit;
	}

	public void setUlimit(int ulimit) {
		this.ulimit = ulimit;
	}

	public javax.servlet.http.HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(javax.servlet.http.HttpServletResponse response) {
		this.response = response;
	}

	public javax.servlet.http.HttpServletRequest getReq() {
		return req;
	}

	public void setReq(javax.servlet.http.HttpServletRequest req) {
		this.req = req;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getPageTileName() {
		return pageTileName;
	}

	public void setPageTileName(String pageTileName) {
		this.pageTileName = pageTileName;
	}

	public int getQueryState() {
		return queryState;
	}

	public void setQueryState(int queryState) {
		this.queryState = queryState;
	}

	public String getStarttime() {
		return starttime;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

	public String getEndtime() {
		return endtime;
	}

	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}

	public GamblingCriminalMan getGamblingCriminalMan() {
		return gamblingCriminalMan;
	}

	public void setGamblingCriminalMan(GamblingCriminalMan gamblingCriminalMan) {
		this.gamblingCriminalMan = gamblingCriminalMan;
	}

	public IGamblingCriminalManService getGamblingCriminalManService() {
		return gamblingCriminalManService;
	}

	@Resource
	public void setGamblingCriminalManService(
			IGamblingCriminalManService gamblingCriminalManService) {
		this.gamblingCriminalManService = gamblingCriminalManService;
	}

	public File getPicture() {
		return picture;
	}

	public void setPicture(File picture) {
		this.picture = picture;
	}

	public String getPictureContentType() {
		return pictureContentType;
	}

	public void setPictureContentType(String pictureContentType) {
		this.pictureContentType = pictureContentType;
	}

	public String getPictureFileName() {
		return pictureFileName;
	}

	public void setPictureFileName(String pictureFileName) {
		this.pictureFileName = pictureFileName;
	}

	public List<String> getInfoExtractions() {
		return infoExtractions;
	}

	public void setInfoExtractions(List<String> infoExtractions) {
		this.infoExtractions = infoExtractions;
	}

	public ILawcaseService getLawcaseService() {
		return lawcaseService;
	}

	@Resource
	public void setLawcaseService(ILawcaseService lawcaseService) {
		this.lawcaseService = lawcaseService;
	}

	public Lawcase getLawcase() {
		return lawcase;
	}

	public void setLawcase(Lawcase lawcase) {
		this.lawcase = lawcase;
	}

	public int getLawid() {
		return lawid;
	}

	public void setLawid(int lawid) {
		this.lawid = lawid;
	}

	public int getTroubid() {
		return troubid;
	}

	public void setTroubid(int troubid) {
		this.troubid = troubid;
	}

	public ITroubleshootingService getTroubleshootingService() {
		return troubleshootingService;
	}

	@Resource
	public void setTroubleshootingService(
			ITroubleshootingService troubleshootingService) {
		this.troubleshootingService = troubleshootingService;
	}

	public Troubleshooting getTroubleshooting() {
		return troubleshooting;
	}

	public void setTroubleshooting(Troubleshooting troubleshooting) {
		this.troubleshooting = troubleshooting;
	}

	public int getJid() {
		return jid;
	}

	public void setJid(int jid) {
		this.jid = jid;
	}

	public IJudgeService getJudgeService() {
		return judgeService;
	}

	@Resource
	public void setJudgeService(IJudgeService judgeService) {
		this.judgeService = judgeService;
	}

	public Judge getJudge() {
		return judge;
	}

	public void setJudge(Judge judge) {
		this.judge = judge;
	}

	public int getSize() {
		return size;
	}

	public String getJsonUnits() {
		return jsonUnits;
	}

	public void setJsonUnits(String jsonUnits) {
		this.jsonUnits = jsonUnits;
	}

	public IUnitService getUnitService() {
		return unitService;
	}

	@Resource
	public void setUnitService(IUnitService unitService) {
		this.unitService = unitService;
	}

	public UnitVO getUnitVO() {
		return unitVO;
	}

	public void setUnitVO(UnitVO unitVO) {
		this.unitVO = unitVO;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public List<Unit> getUnits() {
		return units;
	}

	public void setUnits(List<Unit> units) {
		this.units = units;
	}

	public void setUnitVOs(List<UnitVO> unitVOs) {
		this.unitVOs = unitVOs;
	}

	public List<Judge> getJudges() {
		return judges;
	}

	public void setJudges(List<Judge> judges) {
		this.judges = judges;
	}

	public IGuiltSafeguardManService getGuiltSafeguardManService() {
		return guiltSafeguardManService;
	}

	@Resource
	public void setGuiltSafeguardManService(
			IGuiltSafeguardManService guiltSafeguardManService) {
		this.guiltSafeguardManService = guiltSafeguardManService;
	}

	public GuiltSafeguardMan getGuiltSafeguardMan() {
		return guiltSafeguardMan;
	}

	public void setGuiltSafeguardMan(GuiltSafeguardMan guiltSafeguardMan) {
		this.guiltSafeguardMan = guiltSafeguardMan;
	}

	public IDisappearManService getDisappearmanService() {
		return disappearmanService;
	}

	@Resource
	public void setDisappearmanService(IDisappearManService disappearmanService) {
		this.disappearmanService = disappearmanService;
	}

	public DisappearMan getDisappearman() {
		return disappearman;
	}

	public void setDisappearman(DisappearMan disappearman) {
		this.disappearman = disappearman;
	}

	public IAnalyzeManService getAnalyzeManService() {
		return analyzeManService;
	}

	@Resource
	public void setAnalyzeManService(IAnalyzeManService analyzeManService) {
		this.analyzeManService = analyzeManService;
	}

	public IContrastManService getContrastManService() {
		return contrastManService;
	}

	@Resource
	public void setContrastManService(IContrastManService contrastManService) {
		this.contrastManService = contrastManService;
	}

	public ICommonClueService getCommonClueService() {
		return commonClueService;
	}

	@Resource
	public void setCommonClueService(ICommonClueService commonClueService) {
		this.commonClueService = commonClueService;
	}

	public AnalyzeMan getAnalyzeMan() {
		return analyzeMan;
	}

	public void setAnalyzeMan(AnalyzeMan analyzeMan) {
		this.analyzeMan = analyzeMan;
	}

	public ContrastMan getContrastMan() {
		return contrastMan;
	}

	public void setContrastMan(ContrastMan contrastMan) {
		this.contrastMan = contrastMan;
	}

	public CommonClue getCommonClue() {
		return commonClue;
	}

	public void setCommonClue(CommonClue commonClue) {
		this.commonClue = commonClue;
	}

	public File getPicture1() {
		return picture1;
	}

	public void setPicture1(File picture1) {
		this.picture1 = picture1;
	}

	public String getPicture1ContentType() {
		return picture1ContentType;
	}

	public void setPicture1ContentType(String picture1ContentType) {
		this.picture1ContentType = picture1ContentType;
	}

	public String getPicture1FileName() {
		return picture1FileName;
	}

	public void setPicture1FileName(String picture1FileName) {
		this.picture1FileName = picture1FileName;
	}

	public File getPicture2() {
		return picture2;
	}

	public void setPicture2(File picture2) {
		this.picture2 = picture2;
	}

	public String getPicture2ContentType() {
		return picture2ContentType;
	}

	public void setPicture2ContentType(String picture2ContentType) {
		this.picture2ContentType = picture2ContentType;
	}

	public String getPicture2FileName() {
		return picture2FileName;
	}

	public void setPicture2FileName(String picture2FileName) {
		this.picture2FileName = picture2FileName;
	}

	public File getPicture3() {
		return picture3;
	}

	public void setPicture3(File picture3) {
		this.picture3 = picture3;
	}

	public String getPicture3ContentType() {
		return picture3ContentType;
	}

	public void setPicture3ContentType(String picture3ContentType) {
		this.picture3ContentType = picture3ContentType;
	}

	public String getPicture3FileName() {
		return picture3FileName;
	}

	public void setPicture3FileName(String picture3FileName) {
		this.picture3FileName = picture3FileName;
	}

	public int getOtherid() {
		return otherid;
	}

	public void setOtherid(int otherid) {
		this.otherid = otherid;
	}

	public IOtherpersonService getOtherpersonService() {
		return otherpersonService;
	}

	@Resource
	public void setOtherpersonService(IOtherpersonService otherpersonService) {
		this.otherpersonService = otherpersonService;
	}

	public Otherperson getOtherperson() {
		return otherperson;
	}

	public void setOtherperson(Otherperson otherperson) {
		this.otherperson = otherperson;
	}

	public int getOtype() {
		return otype;
	}

	public void setOtype(int otype) {
		this.otype = otype;
	}

	public List<Otherperson> getGxrs() {
		return gxrs;
	}

	public void setGxrs(List<Otherperson> gxrs) {
		this.gxrs = gxrs;
	}

	public List<Otherperson> getTars() {
		return tars;
	}

	public void setTars(List<Otherperson> tars) {
		this.tars = tars;
	}

	public List<Otherperson> getXyrs() {
		return xyrs;
	}

	public void setXyrs(List<Otherperson> xyrs) {
		this.xyrs = xyrs;
	}

	public List<Otherperson> getOtherpersons() {
		return otherpersons;
	}

	public void setOtherpersons(List<Otherperson> otherpersons) {
		this.otherpersons = otherpersons;
	}

	public int getJtype() {
		return jtype;
	}

	public void setJtype(int jtype) {
		this.jtype = jtype;
	}

	public ISuccessexampleService getSuccessexampleService() {
		return successexampleService;
	}

	@Resource
	public void setSuccessexampleService(
			ISuccessexampleService successexampleService) {
		this.successexampleService = successexampleService;
	}

	public Successexample getSuccessexample() {
		return successexample;
	}

	public void setSuccessexample(Successexample successexample) {
		this.successexample = successexample;
	}

	public IInjurycaseService getInjurycaseService() {
		return injurycaseService;
	}

	@Resource
	public void setInjurycaseService(IInjurycaseService injurycaseService) {
		this.injurycaseService = injurycaseService;
	}

	public IUserRoleService getUserRoleService() {
		return userRoleService;
	}

	@Resource
	public void setUserRoleService(IUserRoleService userRoleService) {
		this.userRoleService = userRoleService;
	}

	public List<UnitVO> getUnitVOs() {
		return unitVOs;
	}

	public String importExcel() {
		return "importpage";
	}

	public String importdata() throws Exception {
		UserRole userRoleo = (UserRole) session.get("userRoleo");
		if (userRoleo == null) {
			return "opsessiongo";
		}

		personService.saveSocialManWithExcel(file, userRoleo);

		return "importdata";
	}

	public SocialManForm getSocialManForm() {
		return socialManForm;
	}

	public void setSocialManForm(SocialManForm socialManForm) {
		this.socialManForm = socialManForm;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getFileContentType() {
		return fileContentType;
	}

	public void setFileContentType(String fileContentType) {
		this.fileContentType = fileContentType;
	}

	public String getFileFileName() {
		return fileFileName;
	}

	public void setFileFileName(String fileFileName) {
		this.fileFileName = fileFileName;
	}

}
