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
			export_url : "/ezScrum/resource/export/projects",
//			items		: myCheckboxGroup,
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
	},
    doExport: function() {
		var obj = this;
    	var form = this.getForm();
    	var form = Ext.DomHelper.append(document.body, {
	        tag : 'form',
	        method : 'get',
	        action : obj.export_url
	    });
	    form.submit();
	    Ext.example.msg('Success', 'Export Projects Success');
	}
});

Ext.reg('ExportForm', ExportFormLayout);