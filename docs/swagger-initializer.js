window.onload = function() {
  //

  window.ui = SwaggerUIBundle({
    url: './openapi.yaml',
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout",

    // Enhanced configuration for examples display
    defaultModelsExpandDepth: 2,
    defaultModelExpandDepth: 2,
    defaultModelRendering: 'example',
    displayOperationId: false,
    displayRequestDuration: true,
    docExpansion: "list",
    filter: true,
    showExtensions: true,
    showCommonExtensions: true,
    tryItOutEnabled: true,
    validatorUrl: null,
    supportedSubmitMethods: ['get', 'post', 'put', 'delete', 'patch', 'head', 'options'],

    // Request/Response interceptors for debugging
    requestInterceptor: (request) => {
      console.log('Swagger UI Request:', request);
      // Add API key if needed
      if (!request.headers['Authorization']) {
        request.headers['Authorization'] = 'Bearer <token>';
      }
      return request;
    },

    responseInterceptor: (response) => {
      console.log('Swagger UI Response:', response);
      return response;
    },

    // Custom configuration for better UX
    operationsSorter: "alpha",
    tagsSorter: "alpha",

    // Show request/response examples
    showRequestHeaders: true,
    showResponseHeaders: true,

    // OAuth2 configuration (if needed)
    oauth2RedirectUrl: window.location.origin + '/oauth2-redirect.html',

    onComplete: function() {
      console.log('Swagger UI loaded successfully');
      console.log('Available operations:', Object.keys(window.ui.getSystem().specActions.spec.json.paths || {}));
    },

    onFailure: function(data) {
      console.error('Failed to load Swagger UI:', data);
    }
  });

  //
};