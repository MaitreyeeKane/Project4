package in.co.sunrays.proj4.controller;

import in.co.sunrays.proj4.bean.BaseBean;
import in.co.sunrays.proj4.bean.RoleBean;
import in.co.sunrays.proj4.bean.StudentBean;
import in.co.sunrays.proj4.bean.UserBean;
import in.co.sunrays.proj4.exception.ApplicationException;
import in.co.sunrays.proj4.exception.RecordNotFoundException;
import in.co.sunrays.proj4.model.CollegeModel;
import in.co.sunrays.proj4.model.StudentModel;
import in.co.sunrays.proj4.util.DataUtility;
import in.co.sunrays.proj4.util.PropertyReader;
import in.co.sunrays.proj4.util.ServletUtility;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * Student List functionality Controller. Performs operation for list, search
 * and delete operations of Student
 * 
 * @author SunilOS
 * @version 1.0
 * @Copyright (c) SunilOS
 */
@WebServlet(name = "StudentListCtl", urlPatterns = { "/ctl/StudentListCtl" })
public class StudentListCtl extends BaseCtl {

	private static Logger log = Logger.getLogger(StudentListCtl.class);

	@Override
	protected void preload(HttpServletRequest request) {

	StudentModel model=new StudentModel();
	List list=null;
	try {
	       list=	model.list();
	      request.setAttribute("sList", list);
	} catch (ApplicationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
	@Override
	protected BaseBean populateBean(HttpServletRequest request) {
		StudentBean bean = new StudentBean();
		bean.setId(DataUtility.getLong(request.getParameter("sName")));
		bean.setFirstName(DataUtility.getString(request.getParameter("firstName")));
		bean.setLastName(DataUtility.getString(request.getParameter("lastName")));
		bean.setEmail(DataUtility.getString(request.getParameter("email")));
		return bean;
	}

	/**
	 * Display logics inside this Method
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.debug("StudentListCtl doGet Start");
		HttpSession session=request.getSession(true);
	    UserBean uBean=(UserBean)session.getAttribute("user");
	    if(uBean.getRoleId()==RoleBean.STUDENT||uBean.getRoleId()==RoleBean.KIOSK){
	    	ServletUtility.redirect(ORSView.ERROR_CTL, request, response);
	    	return;
	    }
		List list = null;

		int pageNo = 1;
		int pageSize = DataUtility.getInt(PropertyReader.getValue("page.size"));
		String op = DataUtility.getString(request.getParameter("operation"));
		StudentBean bean = (StudentBean) populateBean(request);
		StudentModel model = new StudentModel();

		try {
			try {
				list = model.search(bean, pageNo, pageSize);
			} catch (RecordNotFoundException e) {
				ServletUtility.setErrorMessage("No record found ", request);
			}
			/*
			 * if (list == null || list.size() == 0) {
			 * ServletUtility.setErrorMessage("No record found ", request); }
			 */
			// ServletUtility.setList(list, request);
			ServletUtility.setList(list, request);
			ServletUtility.setPageNo(pageNo, request);
			ServletUtility.setPageSize(pageSize, request);
			ServletUtility.forward(getView(), request, response);
		} catch (ApplicationException e) {
			log.error(e);
			ServletUtility.handleException(e, request, response);
			return;
		}
		log.debug("StudentListCtl doGet End");
	}

	/**
	 * Submit logics inside this Method
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List list = null;
		int pageNo = DataUtility.getInt(request.getParameter("pageNo"));
		int pageSize = DataUtility.getInt(request.getParameter("pageSize"));
		pageNo = (pageNo == 0) ? 1 : pageNo;
		pageSize = (pageSize == 0) ? DataUtility.getInt(PropertyReader.getValue("page.size")) : pageSize;

		log.debug("StudentListCtl doPost Start");
		StudentBean bean = (StudentBean) populateBean(request);
		String op = DataUtility.getString(request.getParameter("operation"));
		StudentModel model = new StudentModel();
		String[] ids = request.getParameterValues("chk_1");
		try {

			if (OP_SEARCH.equalsIgnoreCase(op) || "Next".equalsIgnoreCase(op) || "Previous".equalsIgnoreCase(op)) {

				if (OP_SEARCH.equalsIgnoreCase(op)) {
					pageNo = 1;
				} else if (OP_NEXT.equalsIgnoreCase(op)) {
					pageNo++;
				} else if (OP_PREVIOUS.equalsIgnoreCase(op) && pageNo > 1) {
					pageNo--;
				}
			} else if (OP_NEW.equalsIgnoreCase(op)) {
				ServletUtility.redirect(ORSView.STUDENT_CTL, request, response);
				return;
			} else if (OP_BACK.equalsIgnoreCase(op) || OP_RESET.equalsIgnoreCase(op)) {
				ServletUtility.redirect(ORSView.STUDENT_LIST_CTL, request, response);
				return;
			} else if (OP_DELETE.equalsIgnoreCase(op)) {
				pageNo = 1;
				if (ids != null && ids.length > 0) {
					StudentBean deletebean = new StudentBean();
					for (String id : ids) {
						deletebean.setId(DataUtility.getInt(id));
						model.delete(deletebean);
					}
					ServletUtility.setSuccessMessage("Record successfully deleted", request);
				} else {
					ServletUtility.setErrorMessage("Select at least one record", request);
				}
			}

			
				try {
					list = model.search(bean, pageNo, pageSize);
				} catch (RecordNotFoundException e) {
					if(!OP_DELETE.equalsIgnoreCase(op)){
				ServletUtility.setErrorMessage(e.getMessage(), request);
				}
				}
			
			// ServletUtility.setList(list, request);
			/*
			 * if (list == null || list.size() == 0
			 * &&!OP_DELETE.equalsIgnoreCase(op)) {
			 * ServletUtility.setErrorMessage("No record found ", request); }
			 */
			ServletUtility.setBean(bean, request);
			ServletUtility.setList(list, request);
			ServletUtility.setPageNo(pageNo, request);
			ServletUtility.setPageSize(pageSize, request);
			ServletUtility.forward(getView(), request, response);
		} catch (ApplicationException e) {
			log.error(e);
			ServletUtility.handleException(e, request, response);
			return;
		}
		log.debug("StudentListCtl doGet End");
	}

	@Override
	protected String getView() {
		return ORSView.STUDENT_LIST_VIEW;
	}
}