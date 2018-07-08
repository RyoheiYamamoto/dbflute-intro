<client>
  <h2>DBFlute Client { opts.projectName }</h2>
  <span>for { client.databaseCode }, { client.languageCode }, { client.containerCode }</span>

  <h3>Documents</h3>
  <div class="ui list">
    <div class="item" onclick="{ openSchemaHTML }"><a>SchemaHTML</a></div>
    <div class="item" onclick="{ openHistoryHTML }"><a>HistoryHTML</a></div>
  </div>
  <button class="ui positive button" onclick="{ showDocumentSettingModal }">Edit Document Settings</button>
  <button class="ui primary button" onclick="{ task.bind(this, 'doc') }">Generate Documents (jdbc, doc)</button>

  <h3>Schema Sync Check</h3>
  <button class="ui positive button" onclick="{ showSyncSettingModal }">Edit Sync Check</button>
  <button class="ui primary button">Check Schema (schema-sync-check)</button>

  <su-modal modal="{ generateModal }" class="large" ref="generateModal">
    <div class="description">
      Generating...
    </div>
  </su-modal>

  <su-modal modal="{ documentSettingModal }" class="large" ref="documentSettingModal">
    <form class="ui form">
      <div class="field">
        <label>Alias delimiter in DB comment</label>
        <input type="text" ref="aliasDelimiterInDbComment" placeholder="e.g. :" value="{ opts.modal.documentSetting.aliasDelimiterInDbComment }">
      </div>
      <div class="field">
        <div class="ui checkbox">
          <input type="checkbox" ref="upperCaseBasic" checked="{ opts.modal.documentSetting.upperCaseBasic }">
          <label>Upper case basis</label>
        </div>
      </div>
      <div class="field">
        <div class="ui checkbox">
          <input type="checkbox" ref="dbCommentOnAliasBasis" checked="{ opts.modal.documentSetting.dbCommentOnAliasBasis }">
          <label>DB comment on alias basis</label>
        </div>
      </div>
    </form>
  </su-modal>

  <su-modal modal="{ syncSettingModal }" class="large" ref="syncSettingModal">
    <form class="ui form">
      <div class="required field">
        <label>URL</label>
        <input type="url" ref="url" placeholder="jdbc:mysql://localhost:3306/examplesyncdb">
      </div>
      <div class="field">
        <label>Schema</label>
        <input type="text" ref="schema" placeholder="EXAMPLESYNCDB">
      </div>
      <div class="required field">
        <label>User</label>
        <input type="text" ref="user" placeholder="examplesyncdb">
      </div>
      <div class="field">
        <label>Password</label>
        <input type="password" ref="password" placeholder="">
      </div>
      <div class="field">
        <div class="ui checkbox">
          <input type="checkbox" ref="isSuppressCraftDiff">
          <label>Suppress Craft Diff</label>
        </div>
      </div>
    </form>
  </su-modal>

  <script>
    import _ApiFactory from '../common/factory/ApiFactory.js'
    const ApiFactory = new _ApiFactory()

    let self = this
    this.client = {}

    // ===================================================================================
    //                                                                               Modal
    //                                                                               =====
    this.generateModal = {
      closable: false
    }
    this.documentSettingModal = {
      header: 'Document Settings',
      closable: true,
      buttons:  [
        {
          text: 'OK',
          action: 'editDocumentSettings'
        }
      ],
      documentSetting: {}
    }
    this.syncSettingModal = {
      header: 'Schema Sync Check Settings',
      closable: true,
      buttons:  [
        {
          text: 'OK',
          action: 'editDocumentSettings'
        }
      ]
    }

    this.showDocumentSettingModal = () => {
      self.refs.documentSettingModal.show()
    }

    this.showSyncSettingModal = () => {
      self.refs.syncSettingModal.show()
    }

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    this.prepareCurrentProject = () => {
      ApiFactory.clientOperation(self.opts.projectName).then((response) => {
        self.client = response
        self.update()
      })
      ApiFactory.document(self.opts.projectName).then((response) => {
        self.documentSettingModal.documentSetting = response
        self.update()
      })
    }


    // ===================================================================================
    //                                                                               Task
    //                                                                              ======
    this.task = (task) => {
      self.refs.generateModal.show()
      ApiFactory.task(self.opts.projectName, task).then((success) => {
        self.refs.generateModal.hide()
      })
    }

    // ===================================================================================
    //                                                                           Open HTML
    //                                                                           =========
    this.openSchemaHTML = () => {
      window.open(ffetch.baseUrl + 'api/document/' + self.opts.projectName + '/schemahtml/')
    }

    this.openHistoryHTML = () => {
      window.open(ffetch.baseUrl + 'api/document/' + self.opts.projectName + '/historyhtml/')
    }

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    this.on('mount', () => {
      self.prepareCurrentProject(self.opts.projectName)

      this.refs.documentSettingModal.on('editDocumentSettings', () => {
        let documentStringModalRefs = self.refs.documentSettingModal.refs
        let documentSetting = {
          aliasDelimiterInDbComment: documentStringModalRefs.aliasDelimiterInDbComment.value,
          upperCaseBasic:  documentStringModalRefs.upperCaseBasic.checked,
          dbCommentOnAliasBasis: documentStringModalRefs.dbCommentOnAliasBasis.checked
        }
        ApiFactory.editDocument(self.opts.projectName, documentSetting)
      })
    })
  </script>
</client>
