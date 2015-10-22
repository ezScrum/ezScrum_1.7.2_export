var ExportPage = new Ext.Panel({
	id			: 'Export_Page',
	layout		: 'anchor',
	autoScroll	: true,
	items : [
	    { ref: 'ExportForm_ID', xtype : 'ExportForm' }
	],
	listeners : {
		'show' : function() {
			this.ExportProjectForm_ID.loadDataModel();
		}
	}
});