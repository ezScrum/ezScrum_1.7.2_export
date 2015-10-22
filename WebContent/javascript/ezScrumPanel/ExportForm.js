var myCheckboxGroup = new Ext.form.CheckboxGroup({
    id:'myGroup',
    xtype: 'checkboxgroup',
    fieldLabel: 'Existing Project List',
    itemCls: 'x-check-group-alt',
    // Put all controls in a single column with width 100%
    columns: 1,
    items: [
            {boxLabel: 'Project1', name: 'cb-col-1'},
            {boxLabel: 'Project2', name: 'cb-col-1', checked: true}
        ]
});

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
			url			: 'viewProjectList.do',
			export_url	: 'saveConfiguration.do',	
			items		: myCheckboxGroup,
	        buttons : [{
	        	formBind : true,
	        	scope    : this,
	        	text     : 'Export',
	        	disabled : true,
	        	handler  : this.doExport
	        }]
		}
		
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
		Ext.Ajax.request({
			url: obj.url,
			success : function(response) {
				ProjectStore.loadData(response.responseXML);
				console.log(ProjectStore.data);
				var loadmask = new Ext.LoadMask(obj.getEl(), {msg:"loading info..."});
				loadmask.hide();
			},
			failure : function(){
				var loadmask = new Ext.LoadMask(obj.getEl(), {msg:"loading info..."});
    			loadmask.hide();
				Ext.example.msg('Server Error', 'Sorry, the connection is failure.');
			}
		});
	},
    setDataModel: function(record) {
    	this.getForm().reset();
    	this.getForm().setValues({
			ServerUrl	: record.get('ServerUrl'),
			DBAccount	: record.get('DBAccount'),
			DBType		: record.get('DBType'),
			DBName		: record.get('DBName')
		});
    },
    doExport: function() {
		var obj = this;
    	var form = this.getForm();
    	this.showMask('loading info...');
    	Ext.Ajax.request({
    		url		: obj.export_url,
    		params	: form.getValues(),
    		success	: function(response) {
				var result = response.responseText;
				if (result == "success") {
					Ext.example.msg('Modify DB Config', 'Success.');
				} else {
					Ext.example.msg('Modify DB Config', 'Sorry, the action is failure.');
				}
				obj.closeMask();
    		},
    		failure	: function(response){
    			obj.closeMask();
    			Ext.example.msg('Server Error', 'Sorry, the connection is failure.');
    		}
    	});
	}
});

Ext.reg('ExportForm', ExportFormLayout);