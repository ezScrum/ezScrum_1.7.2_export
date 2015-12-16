var projectArray = [];

var checkGroup = {
		id : 'projectCheckBoxGroup',
        xtype: 'fieldset',
        title: 'Existing Project List',
        items: [{
            xtype: 'checkboxgroup',
            itemCls: 'x-check-group-alt',
            columns: 1,
            items: projectArray, 
            listeners: {
            	 change: function() {
            		 var allUnchecked = true;
            		 for (var i = 0; i < projectArray.length; i++)    
                     {    
                         if(projectArray[i].checked){
                        	 allUnchecked = false;
                         }
                     }
            		 if(allUnchecked){
            			 var exportButton = Ext.getCmp('exportButton');
            			 exportButton.setDisabled(true);
            		 } else {
            			 var exportButton = Ext.getCmp('exportButton');
            			 exportButton.setDisabled(false);
            		 }
               }
            }
        }]
    };

var Project = Ext.data.Record.create(['ID', 'Name', 'Comment', 'ProjectManager', 'CreateDate', 'DemoDate']);

var ProjectReader = new Ext.data.XmlReader({
	   record: 'Project',
	   idPath : 'ID',
	   successProperty: 'Result'
}, Project);

var ProjectStore = new Ext.data.Store({
	fields:[
		{name : 'ID'},
		{name : 'Name'},
		{name : 'Comment'},
		{name : 'ProjectManager'},
		{name : 'CreateDate'},
		{name : 'DemoDate'}
	],
	reader : ProjectReader
});

ExportFormLayout = Ext.extend(Ext.form.FormPanel, {
	id 				: 'Export_Form',
	border			: false,
	frame			: true,
	store			: ProjectStore,
	title			: 'Export',
	bodyStyle		: 'padding:0px',
	labelAlign		: 'right',
	labelWidth		: 150,
	buttonAlign		: 'left',
	monitorValid	: true,
	initComponent	: function() {
		var config = {
			items	   : [checkGroup, {
				id : 'exportButton',
				xtype: 'button',
				formBind : true,
				scope : this,
				text : 'Export',
				disabled : true,
				width : 100,
				handler : this.doExport
			}]
		}
		Ext.Ajax.request({
			url:'viewProjectList.do',
			success : function(response) {
				ProjectStore.loadData(response.responseXML);
				ProjectStore.each(function(record){
					var checkbox = new Ext.form.Checkbox({
						boxLabel : 'Name: ' + record.get("ID") + ', ' + 'Comment: ' + record.get("Comment"),
						id : record.get("ID"),
						name : record.get("Name")
					});
			        projectArray.push(checkbox);
			    });
			},
			failure : function(){
				 Ext.example.msg('Fail', 'Server error.');
			}
		});
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		ExportFormLayout.superclass.initComponent.apply(this, arguments);
	},
	showMask : function(msg) {
		this.loadmask = new Ext.LoadMask(this.getEl(), {msg: msg});
		this.loadmask.show();
	},
	closeMask : function() {
		this.loadmask.hide();
	},
	loadDataModel: function() {
		var obj = this;
		var loadmask = new Ext.LoadMask(this.getEl(), {msg:"loading info..."});
		loadmask.show();
	},
    doExport: function() {
    	var obj = this;
		// Get Entiry
		var jsonArray = [];
		for (var i = 0; i < projectArray.length; i++) {
			if (projectArray[i].checked) {
				var jsonObject = {
					name : projectArray[i].id
				};
				jsonArray.push(jsonObject);
			}
		}
		
		//var form = this.getForm();
//    	var form = Ext.DomHelper.append(document.body, {
//	        tag : 'form',
//	        method : 'post',
//	        action : '/ezScrum/resource/export/projects',
//	        params : jsonArray,
//	        standardSubmit : true
//	    });
//    	form.submit({
//            url: '/ezScrum/resource/export/projects' , 
//            method: 'POST',
//            params : jsonArray,
//            waitMsg:'Saving...',
//            reset: false,
//            failure: function(form, action) {
//            	console.log('fail');
//            },
//            success: function(form, action) {
//            	console.log('success');
//            }
//        });    
//    	form.submit();
//	    Ext.example.msg('Success', 'Export Projects Success');
	    
    	Ext.Ajax.request({
    		  url : '/ezScrum/resource/export/projects',
    		  method: 'POST',
    		  headers: { 'Content-Type': 'application/json'},
    		  params : {},
    		  jsonData: jsonArray,
    		  success: function (response) {
    		      var jsonResp = Ext.util.JSON.decode(response.responseText);
    		      console.log(jsonResp);
    		  },
    		  failure: function (response) {
    		      var jsonResp = Ext.util.JSON.decode(response.responseText);
    		      Ext.Msg.alert("Error",jsonResp.error);
    		  }
    		});
	}, 
	
});

Ext.reg('ExportForm', ExportFormLayout);