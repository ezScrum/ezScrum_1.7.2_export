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
		// Load Projects
		Ext.Ajax.request({
			url:'viewProjectList.do',
			success : function(response) {
				ProjectStore.loadData(response.responseXML);
				ProjectStore.each(function(record){
					var checkbox = new Ext.form.Checkbox({
						boxLabel : '`Name`: ' + record.get("ID") + ', ' + '`Comment`: ' + record.get("Comment"),
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
	downloadFileFromResponse: function(response){
		var disposition = response.getResponseHeader('Content-Disposition');
		var filename = disposition.slice(disposition.indexOf("=") + 1, disposition.length);
		var type = response.getResponseHeader('Content-Type');
		var blob = new Blob([ response.responseText ], {type : type});
		if (typeof window.navigator.msSaveBlob !== 'undefined') {
			window.navigator.msSaveBlob(blob, filename);
		} else {
			var URL = window.URL || window.webkitURL;
			var downloadUrl = URL.createObjectURL(blob);
			if (filename) {
				// use HTML5 a[download] attribute to specify filename
				var a = document.createElement("a");
				// safari doesn't support this yet
				a.href = downloadUrl;
				a.download = filename;
				document.body.appendChild(a);
				a.click();
			}
		}
	},
    doExport: function() {
    	// Progress Bar Dialog
		Ext.MessageBox.show({
			title : 'Please wait',
			msg : 'Exporting projects...',
			progressText : 'Initializing...',
			width : 300,
			progress : true,
			closable : false,
			wait:true,
			waitConfig: {interval:200}
		});
    	var obj = this;
		// Get Entity
		var jsonArray = [];
		for (var i = 0; i < projectArray.length; i++) {
			if (projectArray[i].checked) {
				var jsonObject = {
					name : projectArray[i].id
				};
				jsonArray.push(jsonObject);
			}
		}
    	Ext.Ajax.request({
    		  url : '/ezScrum/resource/export/projects',
    		  method: 'POST',
    		  headers: { 'Content-Type': 'application/json'},
    		  params : {},
    		  jsonData: jsonArray,
    		  timeout: 2 * 60 * 60 * 1000,
    		  success: function (response) {
    		     obj.downloadFileFromResponse(response);
    		     Ext.MessageBox.hide();
    		     Ext.example.msg('Done', 'Your data is downloaded!');
    		  },
    		  failure: function (response) {
    			  Ext.MessageBox.hide();
    		      Ext.Msg.alert("Error", response);
    		  }
    	});
	}, 
	
});

Ext.reg('ExportForm', ExportFormLayout);