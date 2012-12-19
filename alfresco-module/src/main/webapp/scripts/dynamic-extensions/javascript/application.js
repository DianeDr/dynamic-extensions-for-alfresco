// Generated by CoffeeScript 1.3.3
(function() {
  var App;

  App = Em.Application.create({
    rootElement: '#application',
    autoinit: true,
    applicationTitle: 'Dynamic Extensions for Alfresco',
    title: '',
    titleDidChange: (function() {
      var title;
      title = this.get('title');
      if (title) {
        title = "" + title + " - " + (this.get('applicationTitle'));
      } else {
        title = this.get('applicationTitle');
      }
      return document.title = title;
    }).observes('title', 'applicationTitle')
  });

  App.Api = Em.Object.extend({
    baseUri: '/alfresco/service',
    managementInfoUri: (function() {
      return this._uri('/dynamic-extensions/management/info');
    }).property('baseUri'),
    dictionaryUri: (function() {
      return this._uri('/json-api/dictionary');
    }).property('baseUri'),
    dataTypesUri: (function() {
      return this._uri('/json-api/dictionary/data-types');
    }).property('baseUri'),
    namespacesUri: (function() {
      return this._uri('/json-api/dictionary/namespaces');
    }).property('baseUri'),
    _uri: function(relativePath) {
      return this.get('baseUri') + relativePath;
    },
    init: function() {
      return $.ajaxSetup({
        dataFilter: function(data, type) {
          return data.replace(/"\/Date\((\d+)\)\/"/, '$1');
        }
      });
    },
    getManagementInfo: function() {
      var location;
      location = this.get('managementInfoUri');
      return $.getJSON(location).promise();
    },
    getModelDefinitions: function() {
      var location;
      location = "" + (this.get('dictionaryUri')) + "/models";
      return $.getJSON(location).promise();
    },
    getModelMetadata: function(name) {
      var location;
      location = "" + (this.get('dictionaryUri')) + "/models/" + (encodeURI(name)) + "/metadata";
      return $.getJSON(location).promise();
    },
    getClassDefinition: function(name) {
      var location;
      location = "" + (this.get('dictionaryUri')) + "/classes/" + (encodeURI(name));
      return $.getJSON(location).promise();
    },
    getDataTypes: function() {
      var location;
      location = this.get('dataTypesUri');
      return $.getJSON(location).promise();
    },
    getNamespaces: function() {
      var location;
      location = this.get('namespacesUri');
      return $.getJSON(location).promise();
    }
  });

  App.api = App.Api.create();

  (typeof exports !== "undefined" && exports !== null ? exports : this).App = App;

}).call(this);
