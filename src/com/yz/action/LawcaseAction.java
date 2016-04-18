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
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.RequestAware;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.ActionSupport;
import com.yz.model.AnalyzeMan;
import com.yz.model.Clue;
import com.yz.model.CommonClue;
import com.yz.model.ContrastMan;
import com.yz.model.DisappearMan;
import com.yz.model.GamblingCriminalMan;
import com.yz.model.GuiltSafeguardMan;
import com.yz.model.Injurycase;
import com.yz.model.Judge;
import com.yz.model.Lawcase;
import com.yz.model.Otherperson;
import com.yz.model.Person;
import com.yz.model.Successexample;
import com.yz.model.TestModel;
import com.yz.model.Troubleshooting;
import com.yz.model.Unit;
import com.yz.model.UserRole;
import com.yz.service.IAnalyzeManService;
import com.yz.service.IClueService;
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
import com.yz.service.ISuccessexampleService;
import com.yz.service.ITroubleshootingService;
import com.yz.service.IUnitService;
import com.yz.util.ConvertUtil;
import com.yz.util.DateTimeKit;
import com.yz.vo.AjaxMsgVO;
import com.yz.vo.UnitVO;

@Component("lawcaseAction")
@Scope("prototype")
public class LawcaseAction extends ActionSupport implements RequestAware,
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
	private int pid;// 人员id
	private int inid; // 案件id
	private int cid;// 刑侦线索

	private int lawid;// 涉及案件

	// service层对象
	private IPersonService personService;
	private IInjurycaseService injurycaseService;
	private IClueService clueService;

	private ILawcaseService lawcaseService;

	// 单个表对象
	private Person person;
	private Injurycase injurycase;
	private Clue clue;

	private Lawcase lawcase;

	// list表对象
	private List<Person> persons;

	/**
	 * 涉案情况模块
	 */
	public String goToAdd() {
		
		if(pid!=0)
		{
			person = personService.loadById(pid);
		}
		if(cid!=0)
		{
			clue = clueService.loadById(cid);
		}
		
		return "add";
	}

	public String add() throws Exception {
		
		if(lawcase.getPerson()!=null)
		{
			changePersonHandleState(lawcase.getPerson().getId());
		}
		if(lawcase.getClue()!=null)
		{
			changeClueHandleState(lawcase.getClue().getId());
		}
		lawcaseService.add(lawcase);
		return "success_child";
	}

	// 改变人员当前处理状态
	private void changePersonHandleState(int perid) {

		Person per = personService.loadById(perid);
		if (per != null) {
			if (per.getHandleState() == 1) {
				per.setHandleState(2);
				personService.update(per);
			}
		}

	}
	
	// 改变线索当前处理状态
	private void changeClueHandleState(int clid) {

		Clue clue = clueService.loadById(clid);
		if (clue != null) {
			if (clue.getHandleState() == 1) {
				clue.setHandleState(2);
				clueService.update(clue);
			}
		}

	}

	public String deleteLawcase() throws Exception {

		lawcase = lawcaseService.loadById(lawid);
		lawcaseService.delete(lawcase);
		AjaxMsgVO msgVO = new AjaxMsgVO();
		msgVO.setMessage("删除成功.");
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

	public String load() {
		lawcase = lawcaseService.loadById(lawid);
		return "load";
	}

	public String update() {
		lawcaseService.update(lawcase);
		return "success_child";
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

	public IInjurycaseService getInjurycaseService() {
		return injurycaseService;
	}

	@Resource
	public void setInjurycaseService(IInjurycaseService injurycaseService) {
		this.injurycaseService = injurycaseService;
	}

	public int getInid() {
		return inid;
	}

	public void setInid(int inid) {
		this.inid = inid;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public IClueService getClueService() {
		return clueService;
	}

	@Resource
	public void setClueService(IClueService clueService) {
		this.clueService = clueService;
	}

	public Injurycase getInjurycase() {
		return injurycase;
	}

	public void setInjurycase(Injurycase injurycase) {
		this.injurycase = injurycase;
	}

	public Clue getClue() {
		return clue;
	}

	public void setClue(Clue clue) {
		this.clue = clue;
	}
	
	

}