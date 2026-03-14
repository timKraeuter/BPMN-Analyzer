"use strict";
(self["webpackChunkgeneration_ui"] = self["webpackChunkgeneration_ui"] || []).push([["main"],{

/***/ 20092
/*!**********************************!*\
  !*** ./src/app/app.component.ts ***!
  \**********************************/
(__unused_webpack_module, __webpack_exports__, __webpack_require__) {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   AppComponent: () => (/* binding */ AppComponent)
/* harmony export */ });
/* harmony import */ var _home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./node_modules/@babel/runtime/helpers/esm/asyncToGenerator.js */ 89204);
/* harmony import */ var _angular_material_stepper__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/material/stepper */ 56622);
/* harmony import */ var _angular_material_icon__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/material/icon */ 93840);
/* harmony import */ var _angular_material_button__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @angular/material/button */ 84175);
/* harmony import */ var _angular_material_divider__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @angular/material/list */ 14102);
/* harmony import */ var _angular_material_tooltip__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @angular/material/tooltip */ 80640);
/* harmony import */ var _pages_proposition_proposition_component__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ./pages/proposition/proposition.component */ 40307);
/* harmony import */ var _pages_modeling_modeling_component__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! ./pages/modeling/modeling.component */ 74055);
/* harmony import */ var _pages_analysis_analysis_component__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! ./pages/analysis/analysis.component */ 69025);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! @angular/core */ 94363);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! @angular/core */ 36124);
/* harmony import */ var _services_bpmnmodeler_service__WEBPACK_IMPORTED_MODULE_11__ = __webpack_require__(/*! ./services/bpmnmodeler.service */ 91907);
/* harmony import */ var _services_theme_service__WEBPACK_IMPORTED_MODULE_12__ = __webpack_require__(/*! ./services/theme.service */ 70487);
/* harmony import */ var _angular_material_button__WEBPACK_IMPORTED_MODULE_13__ = __webpack_require__(/*! @angular/material/button */ 55326);
/* harmony import */ var _angular_material_tooltip__WEBPACK_IMPORTED_MODULE_14__ = __webpack_require__(/*! @angular/material/tooltip */ 15575);

















const _c0 = ["stepper"];
const _c1 = ["proposition"];
class AppComponent {
  modeler;
  themeService;
  stepper;
  propositionComponent;
  constructor(modeler, themeService) {
    this.modeler = modeler;
    this.themeService = themeService;
  }
  stepChanged(event) {
    var _this = this;
    return (0,_home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__["default"])(function* () {
      if (_this.changedToPropositionStep(event)) {
        yield _this.modeler.updateTokenBPMNModelIfNeeded();
      }
      if (_this.changedToAnalyzeStep(event)) {
        yield _this.propositionComponent.saveCurrentProposition();
        yield _this.modeler.updateViewerBPMNModel();
      }
    })();
  }
  changedToAnalyzeStep(event) {
    return event.selectedIndex === 2;
  }
  changedToPropositionStep(event) {
    return event.selectedIndex === 1;
  }
  stepForward(event) {
    if (event.target && event.target.classList.contains('bio-properties-panel-input')) {
      return;
    }
    this.stepper.next();
  }
  stepBackward(event) {
    if (event.target && event.target.classList.contains('bio-properties-panel-input')) {
      return;
    }
    this.stepper.previous();
  }
  static ɵfac = function AppComponent_Factory(__ngFactoryType__) {
    return new (__ngFactoryType__ || AppComponent)(_angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵdirectiveInject"](_services_bpmnmodeler_service__WEBPACK_IMPORTED_MODULE_11__.BPMNModelerService), _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵdirectiveInject"](_services_theme_service__WEBPACK_IMPORTED_MODULE_12__.ThemeService));
  };
  static ɵcmp = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵdefineComponent"]({
    type: AppComponent,
    selectors: [["app-root"]],
    viewQuery: function AppComponent_Query(rf, ctx) {
      if (rf & 1) {
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵviewQuery"](_c0, 5)(_c1, 5);
      }
      if (rf & 2) {
        let _t;
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵqueryRefresh"](_t = _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵloadQuery"]()) && (ctx.stepper = _t.first);
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵqueryRefresh"](_t = _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵloadQuery"]()) && (ctx.propositionComponent = _t.first);
      }
    },
    hostBindings: function AppComponent_HostBindings(rf, ctx) {
      if (rf & 1) {
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵlistener"]("keydown.ArrowRight", function AppComponent_keydown_ArrowRight_HostBindingHandler($event) {
          return ctx.stepForward($event);
        }, _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵresolveDocument"])("keydown.ArrowLeft", function AppComponent_keydown_ArrowLeft_HostBindingHandler($event) {
          return ctx.stepBackward($event);
        }, _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵresolveDocument"]);
      }
    },
    decls: 39,
    vars: 2,
    consts: [["stepper", ""], ["proposition", ""], [1, "theme-toggle"], ["mat-icon-button", "", "data-testid", "theme-toggle-btn", 3, "click", "matTooltip"], ["animationDuration", "0", "data-testid", "stepper", 3, "selectionChange"], ["label", "Model your BPMN process"], [1, "margin-top"], [1, "step-buttons"], ["mat-raised-button", "", "color", "primary", "matStepperNext", "", "data-testid", "step1-next-btn"], ["iconPositionEnd", ""], ["label", "Add BPMN propositions", "optional", ""], ["mat-raised-button", "", "color", "primary", "matStepperPrevious", "", "data-testid", "step2-prev-btn", 1, "right-margin"], ["mat-raised-button", "", "color", "primary", "matStepperNext", "", "data-testid", "step2-next-btn"], ["label", "Analyze your BPMN process"], ["mat-raised-button", "", "color", "primary", "matStepperPrevious", "", "data-testid", "step3-prev-btn", 1, "right-margin"], ["mat-raised-button", "", "color", "primary", "data-testid", "back-to-start-btn", 3, "click"]],
    template: function AppComponent_Template(rf, ctx) {
      if (rf & 1) {
        const _r1 = _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵgetCurrentView"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementStart"](0, "div", 2)(1, "button", 3);
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵlistener"]("click", function AppComponent_Template_button_click_1_listener() {
          return ctx.themeService.toggle();
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementStart"](2, "mat-icon");
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵtext"](3);
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementEnd"]()()();
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementStart"](4, "mat-stepper", 4, 0);
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵlistener"]("selectionChange", function AppComponent_Template_mat_stepper_selectionChange_4_listener($event) {
          return ctx.stepChanged($event);
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementStart"](6, "mat-step", 5);
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelement"](7, "app-modeling")(8, "mat-divider", 6);
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementStart"](9, "div", 7)(10, "button", 8)(11, "mat-icon", 9);
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵtext"](12, "navigate_next");
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵtext"](13, " Next Step ");
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementEnd"]()()();
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementStart"](14, "mat-step", 10);
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelement"](15, "app-proposition", null, 1)(17, "mat-divider", 6);
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementStart"](18, "div", 7)(19, "button", 11)(20, "mat-icon");
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵtext"](21, "navigate_before");
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵtext"](22, " Previous Step ");
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementStart"](23, "button", 12)(24, "mat-icon", 9);
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵtext"](25, "navigate_next");
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵtext"](26, " Next Step ");
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementEnd"]()()();
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementStart"](27, "mat-step", 13);
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelement"](28, "app-analysis")(29, "mat-divider", 6);
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementStart"](30, "div", 7)(31, "button", 14)(32, "mat-icon");
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵtext"](33, "navigate_before");
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵtext"](34, " Previous Step ");
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementStart"](35, "button", 15);
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵlistener"]("click", function AppComponent_Template_button_click_35_listener() {
          _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵrestoreView"](_r1);
          const stepper_r2 = _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵreference"](5);
          return _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵresetView"](stepper_r2.reset());
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementStart"](36, "mat-icon");
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵtext"](37, "restart_alt");
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵtext"](38, " Back to the start ");
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵelementEnd"]()()()();
      }
      if (rf & 2) {
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵadvance"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵproperty"]("matTooltip", ctx.themeService.isDarkMode ? "Switch to light mode" : "Switch to dark mode");
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵadvance"](2);
        _angular_core__WEBPACK_IMPORTED_MODULE_10__["ɵɵtextInterpolate"](ctx.themeService.isDarkMode ? "light_mode" : "dark_mode");
      }
    },
    dependencies: [_angular_material_stepper__WEBPACK_IMPORTED_MODULE_1__.MatStepperModule, _angular_material_stepper__WEBPACK_IMPORTED_MODULE_1__.MatStep, _angular_material_stepper__WEBPACK_IMPORTED_MODULE_1__.MatStepper, _angular_material_stepper__WEBPACK_IMPORTED_MODULE_1__.MatStepperNext, _angular_material_stepper__WEBPACK_IMPORTED_MODULE_1__.MatStepperPrevious, _angular_material_icon__WEBPACK_IMPORTED_MODULE_2__.MatIconModule, _angular_material_icon__WEBPACK_IMPORTED_MODULE_2__.MatIcon, _angular_material_button__WEBPACK_IMPORTED_MODULE_3__.MatButtonModule, _angular_material_button__WEBPACK_IMPORTED_MODULE_3__.MatButton, _angular_material_button__WEBPACK_IMPORTED_MODULE_13__.MatIconButton, _angular_material_divider__WEBPACK_IMPORTED_MODULE_4__.MatDividerModule, _angular_material_divider__WEBPACK_IMPORTED_MODULE_4__.MatDivider, _angular_material_tooltip__WEBPACK_IMPORTED_MODULE_5__.MatTooltipModule, _angular_material_tooltip__WEBPACK_IMPORTED_MODULE_14__.MatTooltip, _pages_modeling_modeling_component__WEBPACK_IMPORTED_MODULE_7__.ModelingComponent, _pages_proposition_proposition_component__WEBPACK_IMPORTED_MODULE_6__.PropositionComponent, _pages_analysis_analysis_component__WEBPACK_IMPORTED_MODULE_8__.AnalysisComponent],
    styles: [".theme-toggle[_ngcontent-%COMP%] {\n  display: flex;\n  justify-content: flex-end;\n  padding: 4px 8px 0 0;\n}\n\n.mat-stepper-horizontal[_ngcontent-%COMP%] {\n  margin-top: 0;\n}\n\n.mat-mdc-form-field[_ngcontent-%COMP%] {\n  margin-top: 16px;\n}\n\n.margin-top[_ngcontent-%COMP%] {\n  margin-top: 10px;\n}\n\n.step-buttons[_ngcontent-%COMP%] {\n  display: flex;\n  justify-content: flex-end;\n  margin-top: 10px;\n}\n/*# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIndlYnBhY2s6Ly8uL3NyYy9hcHAvYXBwLmNvbXBvbmVudC5zY3NzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiJBQUFBO0VBQ0ksYUFBQTtFQUNBLHlCQUFBO0VBQ0Esb0JBQUE7QUFDSjs7QUFFQTtFQUNJLGFBQUE7QUFDSjs7QUFFQTtFQUNJLGdCQUFBO0FBQ0o7O0FBS0E7RUFDSSxnQkFBQTtBQUZKOztBQUtBO0VBQ0ksYUFBQTtFQUNBLHlCQUFBO0VBQ0EsZ0JBQUE7QUFGSiIsInNvdXJjZXNDb250ZW50IjpbIi50aGVtZS10b2dnbGUge1xuICAgIGRpc3BsYXk6IGZsZXg7XG4gICAganVzdGlmeS1jb250ZW50OiBmbGV4LWVuZDtcbiAgICBwYWRkaW5nOiA0cHggOHB4IDAgMDtcbn1cblxuLm1hdC1zdGVwcGVyLWhvcml6b250YWwge1xuICAgIG1hcmdpbi10b3A6IDA7XG59XG5cbi5tYXQtbWRjLWZvcm0tZmllbGQge1xuICAgIG1hcmdpbi10b3A6IDE2cHg7XG59XG5cbi8vIE5lZWRlZCBhcyBhIGNvbXBvbmVudC1zY29wZWQgb3ZlcnJpZGUgc28gaXQgd2lucyBvdmVyIEFuZ3VsYXIgTWF0ZXJpYWwnc1xuLy8gc3RlcHBlciBzdHlsZXMuIFRoZSBnbG9iYWwgLm1hcmdpbi10b3AgcnVsZSBhbG9uZSBkb2VzIG5vdCBoYXZlIGVub3VnaFxuLy8gc3BlY2lmaWNpdHkgaW5zaWRlIHRoZSBlbXVsYXRlZCBlbmNhcHN1bGF0aW9uLlxuLm1hcmdpbi10b3Age1xuICAgIG1hcmdpbi10b3A6IDEwcHg7XG59XG5cbi5zdGVwLWJ1dHRvbnMge1xuICAgIGRpc3BsYXk6IGZsZXg7XG4gICAganVzdGlmeS1jb250ZW50OiBmbGV4LWVuZDtcbiAgICBtYXJnaW4tdG9wOiAxMHB4O1xufVxuIl0sInNvdXJjZVJvb3QiOiIifQ== */"]
  });
}

/***/ },

/***/ 54873
/*!*************************************************************************!*\
  !*** ./src/app/components/analysis-result/analysis-result.component.ts ***!
  \*************************************************************************/
(__unused_webpack_module, __webpack_exports__, __webpack_require__) {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   AnalysisResultComponent: () => (/* binding */ AnalysisResultComponent)
/* harmony export */ });
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/core */ 37580);
/* harmony import */ var _angular_material_list__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/material/list */ 20943);
/* harmony import */ var _angular_material_icon__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/material/icon */ 93840);
/* harmony import */ var _angular_material_card__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @angular/material/card */ 53777);
/* harmony import */ var _angular_material_progress_spinner__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @angular/material/progress-spinner */ 41134);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @angular/core */ 36124);










function AnalysisResultComponent_Conditional_0_Conditional_5_Template(rf, ctx) {
  if (rf & 1) {
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementStart"](0, "span", 1);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵtext"](1, " The verification will at most take 60 seconds. ");
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelement"](2, "mat-spinner", 2);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementEnd"]();
  }
}
function AnalysisResultComponent_Conditional_0_Conditional_6_Conditional_0_Conditional_4_Template(rf, ctx) {
  if (rf & 1) {
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementStart"](0, "mat-icon", 6);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵtext"](1, "check ");
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementEnd"]();
  }
}
function AnalysisResultComponent_Conditional_0_Conditional_6_Conditional_0_Conditional_5_Template(rf, ctx) {
  if (rf & 1) {
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementStart"](0, "mat-icon", 7);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵtext"](1, "close ");
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementEnd"]();
  }
}
function AnalysisResultComponent_Conditional_0_Conditional_6_Conditional_0_Conditional_8_Template(rf, ctx) {
  if (rf & 1) {
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementStart"](0, "span");
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵtext"](1);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementEnd"]();
  }
  if (rf & 2) {
    const ctx_r0 = _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵnextContext"](4);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵadvance"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵtextInterpolate1"](": ", ctx_r0.ctlPropertyResult().error);
  }
}
function AnalysisResultComponent_Conditional_0_Conditional_6_Conditional_0_Template(rf, ctx) {
  if (rf & 1) {
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementStart"](0, "mat-list", 3)(1, "div", 5);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵtext"](2, "CTL property");
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementEnd"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementStart"](3, "mat-list-item");
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵconditionalCreate"](4, AnalysisResultComponent_Conditional_0_Conditional_6_Conditional_0_Conditional_4_Template, 2, 0, "mat-icon", 6)(5, AnalysisResultComponent_Conditional_0_Conditional_6_Conditional_0_Conditional_5_Template, 2, 0, "mat-icon", 7);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementStart"](6, "div", 8);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵtext"](7);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵconditionalCreate"](8, AnalysisResultComponent_Conditional_0_Conditional_6_Conditional_0_Conditional_8_Template, 2, 1, "span");
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementEnd"]()()();
  }
  if (rf & 2) {
    const ctx_r0 = _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵnextContext"](3);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵadvance"](4);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵconditional"](ctx_r0.ctlPropertyResult().valid ? 4 : 5);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵadvance"](3);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵtextInterpolate1"](" ", ctx_r0.ctlPropertyResult().property, " ");
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵadvance"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵconditional"](ctx_r0.ctlPropertyResult().error ? 8 : -1);
  }
}
function AnalysisResultComponent_Conditional_0_Conditional_6_Conditional_1_For_4_Conditional_1_Template(rf, ctx) {
  if (rf & 1) {
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementStart"](0, "mat-icon", 9);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵtext"](1, "check ");
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementEnd"]();
  }
}
function AnalysisResultComponent_Conditional_0_Conditional_6_Conditional_1_For_4_Conditional_2_Template(rf, ctx) {
  if (rf & 1) {
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementStart"](0, "mat-icon", 10);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵtext"](1, "close ");
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementEnd"]();
  }
}
function AnalysisResultComponent_Conditional_0_Conditional_6_Conditional_1_For_4_Conditional_5_Template(rf, ctx) {
  if (rf & 1) {
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementStart"](0, "span");
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵtext"](1);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementEnd"]();
  }
  if (rf & 2) {
    const property_r2 = _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵnextContext"]().$implicit;
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵadvance"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵtextInterpolate1"]("(", property_r2.additionalInfo, ")");
  }
}
function AnalysisResultComponent_Conditional_0_Conditional_6_Conditional_1_For_4_Template(rf, ctx) {
  if (rf & 1) {
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementStart"](0, "mat-list-item");
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵconditionalCreate"](1, AnalysisResultComponent_Conditional_0_Conditional_6_Conditional_1_For_4_Conditional_1_Template, 2, 0, "mat-icon", 9)(2, AnalysisResultComponent_Conditional_0_Conditional_6_Conditional_1_For_4_Conditional_2_Template, 2, 0, "mat-icon", 10);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementStart"](3, "div", 8);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵtext"](4);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵconditionalCreate"](5, AnalysisResultComponent_Conditional_0_Conditional_6_Conditional_1_For_4_Conditional_5_Template, 2, 1, "span");
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementEnd"]()();
  }
  if (rf & 2) {
    const property_r2 = ctx.$implicit;
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵadvance"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵconditional"](property_r2.valid ? 1 : 2);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵadvance"](3);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵtextInterpolate1"](" ", property_r2.name, " ");
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵadvance"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵconditional"](property_r2.additionalInfo ? 5 : -1);
  }
}
function AnalysisResultComponent_Conditional_0_Conditional_6_Conditional_1_Template(rf, ctx) {
  if (rf & 1) {
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementStart"](0, "mat-list", 4)(1, "div", 5);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵtext"](2, "General BPMN properties");
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementEnd"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵrepeaterCreate"](3, AnalysisResultComponent_Conditional_0_Conditional_6_Conditional_1_For_4_Template, 6, 3, "mat-list-item", null, _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵrepeaterTrackByIdentity"]);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementEnd"]();
  }
  if (rf & 2) {
    const ctx_r0 = _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵnextContext"](3);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵadvance"](3);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵrepeater"](ctx_r0.properties());
  }
}
function AnalysisResultComponent_Conditional_0_Conditional_6_Template(rf, ctx) {
  if (rf & 1) {
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵconditionalCreate"](0, AnalysisResultComponent_Conditional_0_Conditional_6_Conditional_0_Template, 9, 3, "mat-list", 3);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵconditionalCreate"](1, AnalysisResultComponent_Conditional_0_Conditional_6_Conditional_1_Template, 5, 0, "mat-list", 4);
  }
  if (rf & 2) {
    const ctx_r0 = _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵnextContext"](2);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵconditional"](ctx_r0.ctlPropertyResult() ? 0 : -1);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵadvance"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵconditional"](ctx_r0.properties().length > 0 ? 1 : -1);
  }
}
function AnalysisResultComponent_Conditional_0_Template(rf, ctx) {
  if (rf & 1) {
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementStart"](0, "mat-card", 0)(1, "mat-card-header")(2, "mat-card-title");
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵtext"](3, "Analysis results");
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementEnd"]()();
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementStart"](4, "mat-card-content");
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵconditionalCreate"](5, AnalysisResultComponent_Conditional_0_Conditional_5_Template, 3, 0, "span", 1)(6, AnalysisResultComponent_Conditional_0_Conditional_6_Template, 2, 2);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵelementEnd"]()();
  }
  if (rf & 2) {
    const ctx_r0 = _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵnextContext"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵadvance"](5);
    _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵconditional"](ctx_r0.running() ? 5 : 6);
  }
}
class AnalysisResultComponent {
  running = (0,_angular_core__WEBPACK_IMPORTED_MODULE_0__.input)(false, ...(ngDevMode ? [{
    debugName: "running"
  }] : /* istanbul ignore next */[]));
  properties = (0,_angular_core__WEBPACK_IMPORTED_MODULE_0__.input)([], ...(ngDevMode ? [{
    debugName: "properties"
  }] : /* istanbul ignore next */[]));
  ctlPropertyResult = (0,_angular_core__WEBPACK_IMPORTED_MODULE_0__.input)(undefined, ...(ngDevMode ? [{
    debugName: "ctlPropertyResult"
  }] : /* istanbul ignore next */[]));
  static ɵfac = function AnalysisResultComponent_Factory(__ngFactoryType__) {
    return new (__ngFactoryType__ || AnalysisResultComponent)();
  };
  static ɵcmp = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵdefineComponent"]({
    type: AnalysisResultComponent,
    selectors: [["app-analysis-result"]],
    inputs: {
      running: [1, "running"],
      properties: [1, "properties"],
      ctlPropertyResult: [1, "ctlPropertyResult"]
    },
    decls: 1,
    vars: 1,
    consts: [["appearance", "outlined", "data-testid", "analysis-results"], ["data-testid", "verification-spinner"], [1, "margin-top"], ["data-testid", "ctl-results"], ["data-testid", "bpmn-property-results"], ["mat-subheader", ""], ["matListItemIcon", "", "data-testid", "ctl-result-valid", 1, "material-icons", "color_green"], ["matListItemIcon", "", "data-testid", "ctl-result-invalid", 1, "material-icons", "color_red"], ["matListItemTitle", ""], ["matListItemIcon", "", 1, "material-icons", "color_green"], ["matListItemIcon", "", 1, "material-icons", "color_red"]],
    template: function AnalysisResultComponent_Template(rf, ctx) {
      if (rf & 1) {
        _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵconditionalCreate"](0, AnalysisResultComponent_Conditional_0_Template, 7, 1, "mat-card", 0);
      }
      if (rf & 2) {
        _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵconditional"](ctx.running() || ctx.properties().length > 0 || ctx.ctlPropertyResult() ? 0 : -1);
      }
    },
    dependencies: [_angular_material_list__WEBPACK_IMPORTED_MODULE_1__.MatListModule, _angular_material_list__WEBPACK_IMPORTED_MODULE_1__.MatList, _angular_material_list__WEBPACK_IMPORTED_MODULE_1__.MatListItem, _angular_material_list__WEBPACK_IMPORTED_MODULE_1__.MatListItemIcon, _angular_material_list__WEBPACK_IMPORTED_MODULE_1__.MatListSubheaderCssMatStyler, _angular_material_list__WEBPACK_IMPORTED_MODULE_1__.MatListItemTitle, _angular_material_icon__WEBPACK_IMPORTED_MODULE_2__.MatIconModule, _angular_material_icon__WEBPACK_IMPORTED_MODULE_2__.MatIcon, _angular_material_card__WEBPACK_IMPORTED_MODULE_3__.MatCardModule, _angular_material_card__WEBPACK_IMPORTED_MODULE_3__.MatCard, _angular_material_card__WEBPACK_IMPORTED_MODULE_3__.MatCardContent, _angular_material_card__WEBPACK_IMPORTED_MODULE_3__.MatCardHeader, _angular_material_card__WEBPACK_IMPORTED_MODULE_3__.MatCardTitle, _angular_material_progress_spinner__WEBPACK_IMPORTED_MODULE_4__.MatProgressSpinnerModule, _angular_material_progress_spinner__WEBPACK_IMPORTED_MODULE_4__.MatProgressSpinner],
    styles: [".material-icons.color_green[_ngcontent-%COMP%] {\n  color: green;\n}\n\n.material-icons.color_red[_ngcontent-%COMP%] {\n  color: red;\n}\n\n.margin-top[_ngcontent-%COMP%] {\n  margin-top: 20px;\n}\n/*# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIndlYnBhY2s6Ly8uL3NyYy9hcHAvY29tcG9uZW50cy9hbmFseXNpcy1yZXN1bHQvYW5hbHlzaXMtcmVzdWx0LmNvbXBvbmVudC5zY3NzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiJBQUFBO0VBQ0ksWUFBQTtBQUNKOztBQUVBO0VBQ0ksVUFBQTtBQUNKOztBQUVBO0VBQ0ksZ0JBQUE7QUFDSiIsInNvdXJjZXNDb250ZW50IjpbIi5tYXRlcmlhbC1pY29ucy5jb2xvcl9ncmVlbiB7XG4gICAgY29sb3I6IGdyZWVuO1xufVxuXG4ubWF0ZXJpYWwtaWNvbnMuY29sb3JfcmVkIHtcbiAgICBjb2xvcjogcmVkO1xufVxuXG4ubWFyZ2luLXRvcCB7XG4gICAgbWFyZ2luLXRvcDogMjBweDtcbn1cbiJdLCJzb3VyY2VSb290IjoiIn0= */"]
  });
}

/***/ },

/***/ 76289
/*!*********************************************************!*\
  !*** ./src/app/components/diagram/diagram.component.ts ***!
  \*********************************************************/
(__unused_webpack_module, __webpack_exports__, __webpack_require__) {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   DiagramComponent: () => (/* binding */ DiagramComponent)
/* harmony export */ });
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/core */ 37580);
/* harmony import */ var _constants_initial_diagram__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../../constants/initial-diagram */ 93076);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/core */ 36124);
/* harmony import */ var _services_bpmnmodeler_service__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../services/bpmnmodeler.service */ 91907);




const _c0 = ["properties"];
const _c1 = ["diagram"];
class DiagramComponent {
  bpmnModeler;
  modeler;
  properties;
  el;
  viewer = (0,_angular_core__WEBPACK_IMPORTED_MODULE_0__.input)(false, ...(ngDevMode ? [{
    debugName: "viewer"
  }] : /* istanbul ignore next */[]));
  propertiesPanel = (0,_angular_core__WEBPACK_IMPORTED_MODULE_0__.input)(false, ...(ngDevMode ? [{
    debugName: "propertiesPanel"
  }] : /* istanbul ignore next */[]));
  height = (0,_angular_core__WEBPACK_IMPORTED_MODULE_0__.input)('750', ...(ngDevMode ? [{
    debugName: "height"
  }] : /* istanbul ignore next */[]));
  constructor(bpmnModeler) {
    this.bpmnModeler = bpmnModeler;
  }
  ngOnInit() {
    if (this.viewer()) {
      this.modeler = this.bpmnModeler.getViewer();
    } else {
      this.modeler = this.bpmnModeler.getModeler();
      this.modeler.importXML(_constants_initial_diagram__WEBPACK_IMPORTED_MODULE_1__.INITIAL_BPMN_DIAGRAM);
    }
  }
  ngAfterContentInit() {
    this.modeler.attachTo(this.el.nativeElement);
    if (this.propertiesPanel()) {
      const propertiesPanel = this.modeler.get('propertiesPanel');
      propertiesPanel.attachTo(this.properties.nativeElement);
    }
  }
  ngOnDestroy() {
    this.modeler?.detach();
  }
  static ɵfac = function DiagramComponent_Factory(__ngFactoryType__) {
    return new (__ngFactoryType__ || DiagramComponent)(_angular_core__WEBPACK_IMPORTED_MODULE_2__["ɵɵdirectiveInject"](_services_bpmnmodeler_service__WEBPACK_IMPORTED_MODULE_3__.BPMNModelerService));
  };
  static ɵcmp = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_2__["ɵɵdefineComponent"]({
    type: DiagramComponent,
    selectors: [["app-diagram"]],
    viewQuery: function DiagramComponent_Query(rf, ctx) {
      if (rf & 1) {
        _angular_core__WEBPACK_IMPORTED_MODULE_2__["ɵɵviewQuery"](_c0, 7)(_c1, 7);
      }
      if (rf & 2) {
        let _t;
        _angular_core__WEBPACK_IMPORTED_MODULE_2__["ɵɵqueryRefresh"](_t = _angular_core__WEBPACK_IMPORTED_MODULE_2__["ɵɵloadQuery"]()) && (ctx.properties = _t.first);
        _angular_core__WEBPACK_IMPORTED_MODULE_2__["ɵɵqueryRefresh"](_t = _angular_core__WEBPACK_IMPORTED_MODULE_2__["ɵɵloadQuery"]()) && (ctx.el = _t.first);
      }
    },
    inputs: {
      viewer: [1, "viewer"],
      propertiesPanel: [1, "propertiesPanel"],
      height: [1, "height"]
    },
    decls: 5,
    vars: 3,
    consts: [["diagram", ""], ["properties", ""], [1, "diagram-parent", "content", "with-diagram"], [1, "diagram-container", "canvas"], ["id", "properties", 1, "properties-panel-parent", 3, "hidden"]],
    template: function DiagramComponent_Template(rf, ctx) {
      if (rf & 1) {
        _angular_core__WEBPACK_IMPORTED_MODULE_2__["ɵɵdomElementStart"](0, "div", 2);
        _angular_core__WEBPACK_IMPORTED_MODULE_2__["ɵɵdomElement"](1, "div", 3, 0)(3, "div", 4, 1);
        _angular_core__WEBPACK_IMPORTED_MODULE_2__["ɵɵdomElementEnd"]();
      }
      if (rf & 2) {
        _angular_core__WEBPACK_IMPORTED_MODULE_2__["ɵɵstyleProp"]("height", ctx.height(), "px");
        _angular_core__WEBPACK_IMPORTED_MODULE_2__["ɵɵadvance"](3);
        _angular_core__WEBPACK_IMPORTED_MODULE_2__["ɵɵdomProperty"]("hidden", !ctx.propertiesPanel());
      }
    },
    styles: [".diagram-container[_ngcontent-%COMP%] {\n  height: 100%;\n  width: 100%;\n}\n\n.properties-panel-parent[_ngcontent-%COMP%] {\n  width: 20%;\n  border-left: solid 3px #eee;\n}\n\n.dark-theme[_nghost-%COMP%]   .properties-panel-parent[_ngcontent-%COMP%], .dark-theme   [_nghost-%COMP%]   .properties-panel-parent[_ngcontent-%COMP%] {\n  border-left-color: #555555;\n}\n\n.diagram-parent[_ngcontent-%COMP%] {\n  border: solid 3px #eee;\n  position: relative;\n  resize: both;\n  overflow: auto;\n  width: 100%;\n  max-height: 100%;\n  max-width: 100%;\n  display: flex;\n}\n\n.dark-theme[_nghost-%COMP%]   .diagram-parent[_ngcontent-%COMP%], .dark-theme   [_nghost-%COMP%]   .diagram-parent[_ngcontent-%COMP%] {\n  border-color: #555555;\n}\n/*# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIndlYnBhY2s6Ly8uL3NyYy9hcHAvY29tcG9uZW50cy9kaWFncmFtL2RpYWdyYW0uY29tcG9uZW50LnNjc3MiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7RUFDSSxZQUFBO0VBQ0EsV0FBQTtBQUNKOztBQUVBO0VBQ0ksVUFBQTtFQUNBLDJCQUFBO0FBQ0o7O0FBRUE7RUFDSSwwQkFBQTtBQUNKOztBQUVBO0VBQ0ksc0JBQUE7RUFDQSxrQkFBQTtFQUNBLFlBQUE7RUFDQSxjQUFBO0VBQ0EsV0FBQTtFQUNBLGdCQUFBO0VBQ0EsZUFBQTtFQUNBLGFBQUE7QUFDSjs7QUFFQTtFQUNJLHFCQUFBO0FBQ0oiLCJzb3VyY2VzQ29udGVudCI6WyIuZGlhZ3JhbS1jb250YWluZXIge1xuICAgIGhlaWdodDogMTAwJTtcbiAgICB3aWR0aDogMTAwJTtcbn1cblxuLnByb3BlcnRpZXMtcGFuZWwtcGFyZW50IHtcbiAgICB3aWR0aDogMjAlO1xuICAgIGJvcmRlci1sZWZ0OiBzb2xpZCAzcHggI2VlZTtcbn1cblxuOmhvc3QtY29udGV4dCguZGFyay10aGVtZSkgLnByb3BlcnRpZXMtcGFuZWwtcGFyZW50IHtcbiAgICBib3JkZXItbGVmdC1jb2xvcjogIzU1NTU1NTtcbn1cblxuLmRpYWdyYW0tcGFyZW50IHtcbiAgICBib3JkZXI6IHNvbGlkIDNweCAjZWVlO1xuICAgIHBvc2l0aW9uOiByZWxhdGl2ZTtcbiAgICByZXNpemU6IGJvdGg7XG4gICAgb3ZlcmZsb3c6IGF1dG87XG4gICAgd2lkdGg6IDEwMCU7XG4gICAgbWF4LWhlaWdodDogMTAwJTtcbiAgICBtYXgtd2lkdGg6IDEwMCU7XG4gICAgZGlzcGxheTogZmxleDtcbn1cblxuOmhvc3QtY29udGV4dCguZGFyay10aGVtZSkgLmRpYWdyYW0tcGFyZW50IHtcbiAgICBib3JkZXItY29sb3I6ICM1NTU1NTU7XG59XG4iXSwic291cmNlUm9vdCI6IiJ9 */"]
  });
}

/***/ },

/***/ 32525
/*!*********************************************************************************************!*\
  !*** ./src/app/components/rename-proposition-dialog/rename-proposition-dialog.component.ts ***!
  \*********************************************************************************************/
(__unused_webpack_module, __webpack_exports__, __webpack_require__) {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   RenamePropositionDialogComponent: () => (/* binding */ RenamePropositionDialogComponent)
/* harmony export */ });
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/core */ 94363);
/* harmony import */ var _angular_forms__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/forms */ 34456);
/* harmony import */ var _angular_material_dialog__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/material/dialog */ 12587);
/* harmony import */ var _angular_material_form_field__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @angular/material/form-field */ 24950);
/* harmony import */ var _angular_material_input__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @angular/material/input */ 95541);
/* harmony import */ var _angular_material_button__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @angular/material/button */ 84175);
/* harmony import */ var _angular_material_icon__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! @angular/material/icon */ 93840);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! @angular/core */ 36124);
/* harmony import */ var _angular_material_form_field__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! @angular/material/form-field */ 80423);
/* harmony import */ var _angular_material_button__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! @angular/material/button */ 55326);














function RenamePropositionDialogComponent_Conditional_7_Template(rf, ctx) {
  if (rf & 1) {
    const _r1 = _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵgetCurrentView"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵelementStart"](0, "button", 6);
    _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵlistener"]("click", function RenamePropositionDialogComponent_Conditional_7_Template_button_click_0_listener() {
      _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵrestoreView"](_r1);
      const ctx_r1 = _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵnextContext"]();
      return _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵresetView"](ctx_r1.newName = "");
    });
    _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵelementStart"](1, "mat-icon");
    _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵtext"](2, "close");
    _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵelementEnd"]()();
  }
}
class RenamePropositionDialogComponent {
  dialogRef;
  newName;
  data = (0,_angular_core__WEBPACK_IMPORTED_MODULE_0__.inject)(_angular_material_dialog__WEBPACK_IMPORTED_MODULE_2__.MAT_DIALOG_DATA);
  constructor(dialogRef) {
    this.dialogRef = dialogRef;
    this.newName = this.data.proposition.name;
  }
  closeDialog() {
    this.dialogRef.close();
  }
  onDialogClick() {
    this.saveNameAndCloseDialog();
  }
  saveNameAndCloseDialog() {
    this.data.proposition.name = this.newName;
    this.closeDialog();
  }
  stopEventPropagation($event) {
    // Stops event propagation so steps are not changed while inputting.
    if ($event.key === 'ArrowLeft' || $event.key === 'ArrowRight') {
      $event.stopPropagation();
    }
  }
  static ɵfac = function RenamePropositionDialogComponent_Factory(__ngFactoryType__) {
    return new (__ngFactoryType__ || RenamePropositionDialogComponent)(_angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵdirectiveInject"](_angular_material_dialog__WEBPACK_IMPORTED_MODULE_2__.MatDialogRef));
  };
  static ɵcmp = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵdefineComponent"]({
    type: RenamePropositionDialogComponent,
    selectors: [["app-rename-proposition-dialog"]],
    hostBindings: function RenamePropositionDialogComponent_HostBindings(rf, ctx) {
      if (rf & 1) {
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵlistener"]("keyup.Enter", function RenamePropositionDialogComponent_keyup_Enter_HostBindingHandler() {
          return ctx.onDialogClick();
        }, _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵresolveDocument"]);
      }
    },
    decls: 13,
    vars: 2,
    consts: [["mat-dialog-title", ""], ["mat-dialog-content", ""], ["matInput", "", 3, "ngModelChange", "keydown", "ngModel"], ["matSuffix", "", "mat-icon-button", "", "aria-label", "Clear"], ["mat-dialog-actions", ""], ["mat-button", "", "color", "primary", 3, "click"], ["matSuffix", "", "mat-icon-button", "", "aria-label", "Clear", 3, "click"]],
    template: function RenamePropositionDialogComponent_Template(rf, ctx) {
      if (rf & 1) {
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵelementStart"](0, "h1", 0);
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵtext"](1, "Rename proposition");
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵelementStart"](2, "div", 1)(3, "mat-form-field")(4, "mat-label");
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵtext"](5, "Proposition name");
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵelementStart"](6, "input", 2);
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵtwoWayListener"]("ngModelChange", function RenamePropositionDialogComponent_Template_input_ngModelChange_6_listener($event) {
          _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵtwoWayBindingSet"](ctx.newName, $event) || (ctx.newName = $event);
          return $event;
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵlistener"]("keydown", function RenamePropositionDialogComponent_Template_input_keydown_6_listener($event) {
          return ctx.stopEventPropagation($event);
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵconditionalCreate"](7, RenamePropositionDialogComponent_Conditional_7_Template, 3, 0, "button", 3);
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵelementEnd"]()();
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵelementStart"](8, "div", 4)(9, "button", 5);
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵlistener"]("click", function RenamePropositionDialogComponent_Template_button_click_9_listener() {
          return ctx.saveNameAndCloseDialog();
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵtext"](10, " Save ");
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵelementStart"](11, "button", 5);
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵlistener"]("click", function RenamePropositionDialogComponent_Template_button_click_11_listener() {
          return ctx.closeDialog();
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵtext"](12, " Cancel ");
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵelementEnd"]()();
      }
      if (rf & 2) {
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵadvance"](6);
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵtwoWayProperty"]("ngModel", ctx.newName);
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵadvance"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵconditional"](ctx.newName ? 7 : -1);
      }
    },
    dependencies: [_angular_forms__WEBPACK_IMPORTED_MODULE_1__.FormsModule, _angular_forms__WEBPACK_IMPORTED_MODULE_1__.DefaultValueAccessor, _angular_forms__WEBPACK_IMPORTED_MODULE_1__.NgControlStatus, _angular_forms__WEBPACK_IMPORTED_MODULE_1__.NgModel, _angular_material_dialog__WEBPACK_IMPORTED_MODULE_2__.MatDialogModule, _angular_material_dialog__WEBPACK_IMPORTED_MODULE_2__.MatDialogTitle, _angular_material_dialog__WEBPACK_IMPORTED_MODULE_2__.MatDialogActions, _angular_material_dialog__WEBPACK_IMPORTED_MODULE_2__.MatDialogContent, _angular_material_form_field__WEBPACK_IMPORTED_MODULE_3__.MatFormFieldModule, _angular_material_form_field__WEBPACK_IMPORTED_MODULE_8__.MatFormField, _angular_material_form_field__WEBPACK_IMPORTED_MODULE_8__.MatLabel, _angular_material_form_field__WEBPACK_IMPORTED_MODULE_8__.MatSuffix, _angular_material_input__WEBPACK_IMPORTED_MODULE_4__.MatInputModule, _angular_material_input__WEBPACK_IMPORTED_MODULE_4__.MatInput, _angular_material_button__WEBPACK_IMPORTED_MODULE_5__.MatButtonModule, _angular_material_button__WEBPACK_IMPORTED_MODULE_5__.MatButton, _angular_material_button__WEBPACK_IMPORTED_MODULE_9__.MatIconButton, _angular_material_icon__WEBPACK_IMPORTED_MODULE_6__.MatIconModule, _angular_material_icon__WEBPACK_IMPORTED_MODULE_6__.MatIcon],
    styles: ["button[_ngcontent-%COMP%] {\n  margin-right: 8px;\n}\n/*# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIndlYnBhY2s6Ly8uL3NyYy9hcHAvY29tcG9uZW50cy9yZW5hbWUtcHJvcG9zaXRpb24tZGlhbG9nL3JlbmFtZS1wcm9wb3NpdGlvbi1kaWFsb2cuY29tcG9uZW50LnNjc3MiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7RUFDSSxpQkFBQTtBQUNKIiwic291cmNlc0NvbnRlbnQiOlsiYnV0dG9uIHtcbiAgICBtYXJnaW4tcmlnaHQ6IDhweDtcbn1cbiJdLCJzb3VyY2VSb290IjoiIn0= */"]
  });
}

/***/ },

/***/ 70945
/*!*************************************************************************************!*\
  !*** ./src/app/components/temporal-logic-syntax/temporal-logic-syntax.component.ts ***!
  \*************************************************************************************/
(__unused_webpack_module, __webpack_exports__, __webpack_require__) {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   TemporalLogicSyntaxComponent: () => (/* binding */ TemporalLogicSyntaxComponent)
/* harmony export */ });
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/core */ 36124);

class TemporalLogicSyntaxComponent {
  static ɵfac = function TemporalLogicSyntaxComponent_Factory(__ngFactoryType__) {
    return new (__ngFactoryType__ || TemporalLogicSyntaxComponent)();
  };
  static ɵcmp = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdefineComponent"]({
    type: TemporalLogicSyntaxComponent,
    selectors: [["app-temporal-logic-syntax"]],
    decls: 22,
    vars: 0,
    consts: [["href", "https://groove.cs.utwente.nl/manual.html"]],
    template: function TemporalLogicSyntaxComponent_Template(rf, ctx) {
      if (rf & 1) {
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵtext"](0, "Temporal logic model checking is executed in groove. Thus the groove syntax for\ntemporal logic has to be used.\n");
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdomElementStart"](1, "ul")(2, "li");
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵtext"](3, "A for all future paths (CTL)");
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdomElementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdomElementStart"](4, "li");
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵtext"](5, "E for some paths (CTL)");
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdomElementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdomElementStart"](6, "li");
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵtext"](7, "U for until");
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdomElementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdomElementStart"](8, "li");
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵtext"](9, "X for next");
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdomElementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdomElementStart"](10, "li");
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵtext"](11, "W for weak until");
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdomElementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdomElementStart"](12, "li");
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵtext"](13, "G for always (globally)");
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdomElementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdomElementStart"](14, "li");
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵtext"](15, "F for finally");
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdomElementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdomElementStart"](16, "li");
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵtext"](17, "Boolean operators: &, |, !, ->, <-, <->, true and false.");
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdomElementEnd"]()();
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵtext"](18, "\nA full description can be found in chapter 4.2 of the groove user manual\navailable ");
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdomElementStart"](19, "a", 0);
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵtext"](20, "here");
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdomElementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵtext"](21, ".\n");
      }
    },
    encapsulation: 2
  });
}

/***/ },

/***/ 90405
/*!*********************************************************************!*\
  !*** ./src/app/components/token-diagram/token-diagram.component.ts ***!
  \*********************************************************************/
(__unused_webpack_module, __webpack_exports__, __webpack_require__) {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   TokenDiagramComponent: () => (/* binding */ TokenDiagramComponent)
/* harmony export */ });
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/core */ 37580);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ 36124);
/* harmony import */ var _services_bpmnmodeler_service__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../services/bpmnmodeler.service */ 91907);



const _c0 = ["ref"];
class TokenDiagramComponent {
  bpmnModeler;
  modeler;
  el;
  viewer = (0,_angular_core__WEBPACK_IMPORTED_MODULE_0__.input)(false, ...(ngDevMode ? [{
    debugName: "viewer"
  }] : /* istanbul ignore next */[]));
  constructor(bpmnModeler) {
    this.bpmnModeler = bpmnModeler;
    this.modeler = bpmnModeler.getTokenModeler();
  }
  ngAfterContentInit() {
    this.modeler.attachTo(this.el.nativeElement);
  }
  ngOnDestroy() {
    this.modeler.detach();
  }
  static ɵfac = function TokenDiagramComponent_Factory(__ngFactoryType__) {
    return new (__ngFactoryType__ || TokenDiagramComponent)(_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdirectiveInject"](_services_bpmnmodeler_service__WEBPACK_IMPORTED_MODULE_2__.BPMNModelerService));
  };
  static ɵcmp = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineComponent"]({
    type: TokenDiagramComponent,
    selectors: [["app-token-diagram"]],
    viewQuery: function TokenDiagramComponent_Query(rf, ctx) {
      if (rf & 1) {
        _angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵviewQuery"](_c0, 7);
      }
      if (rf & 2) {
        let _t;
        _angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵqueryRefresh"](_t = _angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵloadQuery"]()) && (ctx.el = _t.first);
      }
    },
    inputs: {
      viewer: [1, "viewer"]
    },
    decls: 2,
    vars: 0,
    consts: [["ref", ""], [1, "diagram-container"]],
    template: function TokenDiagramComponent_Template(rf, ctx) {
      if (rf & 1) {
        _angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdomElement"](0, "div", 1, 0);
      }
    },
    styles: [".diagram-container[_ngcontent-%COMP%] {\n  height: 100%;\n  width: 100%;\n}\n/*# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIndlYnBhY2s6Ly8uL3NyYy9hcHAvY29tcG9uZW50cy90b2tlbi1kaWFncmFtL3Rva2VuLWRpYWdyYW0uY29tcG9uZW50LnRzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiJBQUNZO0VBQ0ksWUFBQTtFQUNBLFdBQUE7QUFBaEIiLCJzb3VyY2VzQ29udGVudCI6WyJcbiAgICAgICAgICAgIC5kaWFncmFtLWNvbnRhaW5lciB7XG4gICAgICAgICAgICAgICAgaGVpZ2h0OiAxMDAlO1xuICAgICAgICAgICAgICAgIHdpZHRoOiAxMDAlO1xuICAgICAgICAgICAgfVxuICAgICAgICAiXSwic291cmNlUm9vdCI6IiJ9 */"]
  });
}

/***/ },

/***/ 93076
/*!**********************************************!*\
  !*** ./src/app/constants/initial-diagram.ts ***!
  \**********************************************/
(__unused_webpack_module, __webpack_exports__, __webpack_require__) {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   INITIAL_BPMN_DIAGRAM: () => (/* binding */ INITIAL_BPMN_DIAGRAM)
/* harmony export */ });
/** Default BPMN diagram XML displayed when the modeler initializes. */
const INITIAL_BPMN_DIAGRAM = '<?xml version="1.0" encoding="UTF-8"?>\n' + '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC" xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" targetNamespace="" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL http://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd">\n' + '  <collaboration id="Collaboration">\n' + '    <participant id="Customer" name="Order handling" processRef="Customer_1" />\n' + '  </collaboration>\n' + '  <process id="Customer_1" name="Customer" processType="None" isClosed="false" isExecutable="false">\n' + '    <extensionElements />\n' + '    <laneSet id="sid-b167d0d7-e761-4636-9200-76b7f0e8e83a">\n' + '      <lane id="lane">\n' + '        <flowNodeRef>start-event</flowNodeRef>\n' + '        <flowNodeRef>Event_0qcvv2g</flowNodeRef>\n' + '        <flowNodeRef>Activity_0lgvp3u</flowNodeRef>\n' + '        <flowNodeRef>Gateway_1x8m4ws</flowNodeRef>\n' + '        <flowNodeRef>Activity_1up8xq1</flowNodeRef>\n' + '        <flowNodeRef>Activity_1jgyh05</flowNodeRef>\n' + '        <flowNodeRef>Gateway_0eef44j</flowNodeRef>\n' + '      </lane>\n' + '    </laneSet>\n' + '    <startEvent id="start-event" name="Order placed">\n' + '      <outgoing>Flow_0wq8dog</outgoing>\n' + '    </startEvent>\n' + '    <sequenceFlow id="Flow_0wq8dog" sourceRef="start-event" targetRef="Gateway_0eef44j" />\n' + '    <sequenceFlow id="Flow_0u9a0g3" sourceRef="Gateway_0eef44j" targetRef="Activity_1jgyh05" />\n' + '    <sequenceFlow id="Flow_1mtm8jg" sourceRef="Gateway_0eef44j" targetRef="Activity_1up8xq1" />\n' + '    <sequenceFlow id="Flow_1flhoxp" sourceRef="Activity_1jgyh05" targetRef="Gateway_1x8m4ws" />\n' + '    <sequenceFlow id="Flow_1n9ng49" sourceRef="Activity_1up8xq1" targetRef="Gateway_1x8m4ws" />\n' + '    <sequenceFlow id="Flow_14i9c18" sourceRef="Gateway_1x8m4ws" targetRef="Activity_0lgvp3u" />\n' + '    <sequenceFlow id="Flow_14fhivy" sourceRef="Activity_0lgvp3u" targetRef="Event_0qcvv2g" />\n' + '    <endEvent id="Event_0qcvv2g" name="Order delivered">\n' + '      <incoming>Flow_14fhivy</incoming>\n' + '    </endEvent>\n' + '    <userTask id="Activity_0lgvp3u" name="Ship goods">\n' + '      <incoming>Flow_14i9c18</incoming>\n' + '      <outgoing>Flow_14fhivy</outgoing>\n' + '    </userTask>\n' + '    <exclusiveGateway id="Gateway_1x8m4ws">\n' + '      <incoming>Flow_1flhoxp</incoming>\n' + '      <incoming>Flow_1n9ng49</incoming>\n' + '      <outgoing>Flow_14i9c18</outgoing>\n' + '    </exclusiveGateway>\n' + '    <userTask id="Activity_1up8xq1" name="Fetch goods">\n' + '      <incoming>Flow_1mtm8jg</incoming>\n' + '      <outgoing>Flow_1n9ng49</outgoing>\n' + '    </userTask>\n' + '    <serviceTask id="Activity_1jgyh05" name="Retrieve payment">\n' + '      <incoming>Flow_0u9a0g3</incoming>\n' + '      <outgoing>Flow_1flhoxp</outgoing>\n' + '    </serviceTask>\n' + '    <parallelGateway id="Gateway_0eef44j">\n' + '      <incoming>Flow_0wq8dog</incoming>\n' + '      <outgoing>Flow_0u9a0g3</outgoing>\n' + '      <outgoing>Flow_1mtm8jg</outgoing>\n' + '    </parallelGateway>\n' + '  </process>\n' + '  <bpmndi:BPMNDiagram id="sid-74620812-92c4-44e5-949c-aa47393d3830">\n' + '    <bpmndi:BPMNPlane id="sid-cdcae759-2af7-4a6d-bd02-53f3352a731d" bpmnElement="Collaboration">\n' + '      <bpmndi:BPMNShape id="sid-87F4C1D6-25E1-4A45-9DA7-AD945993D06F_gui" bpmnElement="Customer" isHorizontal="true">\n' + '        <omgdc:Bounds x="170" y="30" width="720" height="260" />\n' + '        <bpmndi:BPMNLabel labelStyle="sid-84cb49fd-2f7c-44fb-8950-83c3fa153d3b" />\n' + '      </bpmndi:BPMNShape>\n' + '      <bpmndi:BPMNShape id="sid-57E4FE0D-18E4-478D-BC5D-B15164E93254_gui" bpmnElement="lane" isHorizontal="true">\n' + '        <omgdc:Bounds x="200" y="30" width="690" height="260" />\n' + '      </bpmndi:BPMNShape>\n' + '      <bpmndi:BPMNShape id="StartEvent_0l6sgn0_di" bpmnElement="start-event">\n' + '        <omgdc:Bounds x="221" y="102" width="36" height="36" />\n' + '        <bpmndi:BPMNLabel>\n' + '          <omgdc:Bounds x="208" y="139" width="64" height="14" />\n' + '        </bpmndi:BPMNLabel>\n' + '      </bpmndi:BPMNShape>\n' + '      <bpmndi:BPMNShape id="Event_0qcvv2g_di" bpmnElement="Event_0qcvv2g">\n' + '        <omgdc:Bounds x="772" y="102" width="36" height="36" />\n' + '        <bpmndi:BPMNLabel>\n' + '          <omgdc:Bounds x="752" y="145" width="76" height="14" />\n' + '        </bpmndi:BPMNLabel>\n' + '      </bpmndi:BPMNShape>\n' + '      <bpmndi:BPMNShape id="Activity_1w8qyby_di" bpmnElement="Activity_0lgvp3u">\n' + '        <omgdc:Bounds x="630" y="80" width="100" height="80" />\n' + '      </bpmndi:BPMNShape>\n' + '      <bpmndi:BPMNShape id="Gateway_0j0fumn_di" bpmnElement="Gateway_1x8m4ws">\n' + '        <omgdc:Bounds x="545" y="95" width="50" height="50" />\n' + '      </bpmndi:BPMNShape>\n' + '      <bpmndi:BPMNShape id="Activity_11kif52_di" bpmnElement="Activity_1up8xq1">\n' + '        <omgdc:Bounds x="380" y="190" width="100" height="80" />\n' + '      </bpmndi:BPMNShape>\n' + '      <bpmndi:BPMNShape id="Activity_1oob6z8_di" bpmnElement="Activity_1jgyh05">\n' + '        <omgdc:Bounds x="380" y="80" width="100" height="80" />\n' + '        <bpmndi:BPMNLabel />\n' + '      </bpmndi:BPMNShape>\n' + '      <bpmndi:BPMNShape id="Gateway_1iluv37_di" bpmnElement="Gateway_0eef44j">\n' + '        <omgdc:Bounds x="295" y="95" width="50" height="50" />\n' + '      </bpmndi:BPMNShape>\n' + '      <bpmndi:BPMNEdge id="Flow_0wq8dog_di" bpmnElement="Flow_0wq8dog">\n' + '        <omgdi:waypoint x="257" y="120" />\n' + '        <omgdi:waypoint x="295" y="120" />\n' + '      </bpmndi:BPMNEdge>\n' + '      <bpmndi:BPMNEdge id="Flow_0u9a0g3_di" bpmnElement="Flow_0u9a0g3">\n' + '        <omgdi:waypoint x="345" y="120" />\n' + '        <omgdi:waypoint x="380" y="120" />\n' + '      </bpmndi:BPMNEdge>\n' + '      <bpmndi:BPMNEdge id="Flow_1mtm8jg_di" bpmnElement="Flow_1mtm8jg">\n' + '        <omgdi:waypoint x="320" y="145" />\n' + '        <omgdi:waypoint x="320" y="230" />\n' + '        <omgdi:waypoint x="380" y="230" />\n' + '      </bpmndi:BPMNEdge>\n' + '      <bpmndi:BPMNEdge id="Flow_1flhoxp_di" bpmnElement="Flow_1flhoxp">\n' + '        <omgdi:waypoint x="480" y="120" />\n' + '        <omgdi:waypoint x="545" y="120" />\n' + '      </bpmndi:BPMNEdge>\n' + '      <bpmndi:BPMNEdge id="Flow_1n9ng49_di" bpmnElement="Flow_1n9ng49">\n' + '        <omgdi:waypoint x="480" y="230" />\n' + '        <omgdi:waypoint x="570" y="230" />\n' + '        <omgdi:waypoint x="570" y="145" />\n' + '      </bpmndi:BPMNEdge>\n' + '      <bpmndi:BPMNEdge id="Flow_14i9c18_di" bpmnElement="Flow_14i9c18">\n' + '        <omgdi:waypoint x="595" y="120" />\n' + '        <omgdi:waypoint x="630" y="120" />\n' + '      </bpmndi:BPMNEdge>\n' + '      <bpmndi:BPMNEdge id="Flow_14fhivy_di" bpmnElement="Flow_14fhivy">\n' + '        <omgdi:waypoint x="730" y="120" />\n' + '        <omgdi:waypoint x="772" y="120" />\n' + '      </bpmndi:BPMNEdge>\n' + '    </bpmndi:BPMNPlane>\n' + '    <bpmndi:BPMNLabelStyle id="sid-e0502d32-f8d1-41cf-9c4a-cbb49fecf581">\n' + '      <omgdc:Font name="Arial" size="11" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false" />\n' + '    </bpmndi:BPMNLabelStyle>\n' + '    <bpmndi:BPMNLabelStyle id="sid-84cb49fd-2f7c-44fb-8950-83c3fa153d3b">\n' + '      <omgdc:Font name="Arial" size="12" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false" />\n' + '    </bpmndi:BPMNLabelStyle>\n' + '  </bpmndi:BPMNDiagram>\n' + '</definitions>\n';

/***/ },

/***/ 69025
/*!******************************************************!*\
  !*** ./src/app/pages/analysis/analysis.component.ts ***!
  \******************************************************/
(__unused_webpack_module, __webpack_exports__, __webpack_require__) {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   AnalysisComponent: () => (/* binding */ AnalysisComponent)
/* harmony export */ });
/* harmony import */ var _home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./node_modules/@babel/runtime/helpers/esm/asyncToGenerator.js */ 89204);
/* harmony import */ var _angular_forms__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/forms */ 34456);
/* harmony import */ var rxjs__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! rxjs */ 89475);
/* harmony import */ var _components_analysis_result_analysis_result_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../components/analysis-result/analysis-result.component */ 54873);
/* harmony import */ var _components_temporal_logic_syntax_temporal_logic_syntax_component__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../../components/temporal-logic-syntax/temporal-logic-syntax.component */ 70945);
/* harmony import */ var _angular_material_snack_bar__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @angular/material/snack-bar */ 3347);
/* harmony import */ var _angular_material_card__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! @angular/material/card */ 53777);
/* harmony import */ var _angular_material_button__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! @angular/material/button */ 84175);
/* harmony import */ var _angular_material_icon__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! @angular/material/icon */ 93840);
/* harmony import */ var _angular_material_form_field__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! @angular/material/form-field */ 24950);
/* harmony import */ var _angular_material_input__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! @angular/material/input */ 95541);
/* harmony import */ var _angular_material_select__WEBPACK_IMPORTED_MODULE_11__ = __webpack_require__(/*! @angular/material/select */ 25175);
/* harmony import */ var _angular_material_tooltip__WEBPACK_IMPORTED_MODULE_12__ = __webpack_require__(/*! @angular/material/tooltip */ 80640);
/* harmony import */ var _angular_material_button_toggle__WEBPACK_IMPORTED_MODULE_13__ = __webpack_require__(/*! @angular/material/button-toggle */ 59864);
/* harmony import */ var _angular_material_divider__WEBPACK_IMPORTED_MODULE_14__ = __webpack_require__(/*! @angular/material/list */ 14102);
/* harmony import */ var _angular_material_tabs__WEBPACK_IMPORTED_MODULE_15__ = __webpack_require__(/*! @angular/material/tabs */ 38223);
/* harmony import */ var _angular_material_progress_spinner__WEBPACK_IMPORTED_MODULE_16__ = __webpack_require__(/*! @angular/material/progress-spinner */ 41134);
/* harmony import */ var _components_diagram_diagram_component__WEBPACK_IMPORTED_MODULE_17__ = __webpack_require__(/*! ../../components/diagram/diagram.component */ 76289);
/* harmony import */ var file_saver_es__WEBPACK_IMPORTED_MODULE_18__ = __webpack_require__(/*! file-saver-es */ 46244);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_19__ = __webpack_require__(/*! @angular/core */ 37580);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_20__ = __webpack_require__(/*! @angular/core */ 94363);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_21__ = __webpack_require__(/*! @angular/core */ 36124);
/* harmony import */ var _services_bpmnmodeler_service__WEBPACK_IMPORTED_MODULE_22__ = __webpack_require__(/*! ../../services/bpmnmodeler.service */ 91907);
/* harmony import */ var _services_model_checking_service__WEBPACK_IMPORTED_MODULE_23__ = __webpack_require__(/*! ../../services/model-checking.service */ 15482);
/* harmony import */ var _services_shared_state_service__WEBPACK_IMPORTED_MODULE_24__ = __webpack_require__(/*! ../../services/shared-state.service */ 42927);
/* harmony import */ var _angular_material_button__WEBPACK_IMPORTED_MODULE_25__ = __webpack_require__(/*! @angular/material/button */ 55326);
/* harmony import */ var _angular_material_form_field__WEBPACK_IMPORTED_MODULE_26__ = __webpack_require__(/*! @angular/material/form-field */ 80423);
/* harmony import */ var _angular_material_select__WEBPACK_IMPORTED_MODULE_27__ = __webpack_require__(/*! @angular/material/select */ 18953);
/* harmony import */ var _angular_material_tooltip__WEBPACK_IMPORTED_MODULE_28__ = __webpack_require__(/*! @angular/material/tooltip */ 15575);




































function AnalysisComponent_Conditional_2_Template(rf, ctx) {
  if (rf & 1) {
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](0, "span");
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](1, " Generating a graph transformation system. ");
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelement"](2, "mat-spinner", 14);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
  }
}
function AnalysisComponent_Conditional_3_Template(rf, ctx) {
  if (rf & 1) {
    const _r1 = _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵgetCurrentView"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](0, "button", 26);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵlistener"]("click", function AnalysisComponent_Conditional_3_Template_button_click_0_listener() {
      _angular_core__WEBPACK_IMPORTED_MODULE_20__["ɵɵrestoreView"](_r1);
      const ctx_r1 = _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵnextContext"]();
      return _angular_core__WEBPACK_IMPORTED_MODULE_20__["ɵɵresetView"](ctx_r1.downloadGGClicked());
    });
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](1, "mat-icon");
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](2, "cloud_download");
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](3, " Download GT-system ");
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
  }
}
function AnalysisComponent_For_39_Template(rf, ctx) {
  if (rf & 1) {
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](0, "mat-option", 16);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](1);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
  }
  if (rf & 2) {
    const template_r3 = ctx.$implicit;
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵproperty"]("value", template_r3);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵadvance"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtextInterpolate1"](" ", template_r3.description, " ");
  }
}
function AnalysisComponent_Conditional_40_Conditional_1_Template(rf, ctx) {
  if (rf & 1) {
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](0, "mat-label");
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](1, "Proposition ");
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
  }
}
function AnalysisComponent_Conditional_40_Conditional_2_Template(rf, ctx) {
  if (rf & 1) {
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](0, "mat-label");
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](1, "Proposition 1");
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
  }
}
function AnalysisComponent_Conditional_40_For_5_Template(rf, ctx) {
  if (rf & 1) {
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](0, "mat-option", 16);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](1);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
  }
  if (rf & 2) {
    const proposition_r5 = ctx.$implicit;
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵproperty"]("value", proposition_r5);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵadvance"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtextInterpolate1"](" ", proposition_r5, " ");
  }
}
function AnalysisComponent_Conditional_40_Template(rf, ctx) {
  if (rf & 1) {
    const _r4 = _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵgetCurrentView"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](0, "mat-form-field", 17);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵconditionalCreate"](1, AnalysisComponent_Conditional_40_Conditional_1_Template, 2, 0, "mat-label")(2, AnalysisComponent_Conditional_40_Conditional_2_Template, 2, 0, "mat-label");
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](3, "mat-select", 27);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtwoWayListener"]("ngModelChange", function AnalysisComponent_Conditional_40_Template_mat_select_ngModelChange_3_listener($event) {
      _angular_core__WEBPACK_IMPORTED_MODULE_20__["ɵɵrestoreView"](_r4);
      const ctx_r1 = _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵnextContext"]();
      _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtwoWayBindingSet"](ctx_r1.selectedProposition1, $event) || (ctx_r1.selectedProposition1 = $event);
      return _angular_core__WEBPACK_IMPORTED_MODULE_20__["ɵɵresetView"]($event);
    });
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵrepeaterCreate"](4, AnalysisComponent_Conditional_40_For_5_Template, 2, 2, "mat-option", 16, _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵrepeaterTrackByIdentity"]);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]()();
  }
  if (rf & 2) {
    const ctx_r1 = _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵnextContext"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵadvance"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵconditional"](!ctx_r1.selectedTemplate.twoPropositions ? 1 : 2);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵadvance"](2);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtwoWayProperty"]("ngModel", ctx_r1.selectedProposition1);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵadvance"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵrepeater"](ctx_r1.getPropositions());
  }
}
function AnalysisComponent_Conditional_41_For_5_Template(rf, ctx) {
  if (rf & 1) {
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](0, "mat-option", 16);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](1);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
  }
  if (rf & 2) {
    const proposition_r7 = ctx.$implicit;
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵproperty"]("value", proposition_r7);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵadvance"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtextInterpolate1"](" ", proposition_r7, " ");
  }
}
function AnalysisComponent_Conditional_41_Template(rf, ctx) {
  if (rf & 1) {
    const _r6 = _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵgetCurrentView"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](0, "mat-form-field", 17)(1, "mat-label");
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](2, "Proposition 2");
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](3, "mat-select", 28);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtwoWayListener"]("ngModelChange", function AnalysisComponent_Conditional_41_Template_mat_select_ngModelChange_3_listener($event) {
      _angular_core__WEBPACK_IMPORTED_MODULE_20__["ɵɵrestoreView"](_r6);
      const ctx_r1 = _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵnextContext"]();
      _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtwoWayBindingSet"](ctx_r1.selectedProposition2, $event) || (ctx_r1.selectedProposition2 = $event);
      return _angular_core__WEBPACK_IMPORTED_MODULE_20__["ɵɵresetView"]($event);
    });
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵrepeaterCreate"](4, AnalysisComponent_Conditional_41_For_5_Template, 2, 2, "mat-option", 16, _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵrepeaterTrackByIdentity"]);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]()();
  }
  if (rf & 2) {
    const ctx_r1 = _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵnextContext"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵadvance"](3);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtwoWayProperty"]("ngModel", ctx_r1.selectedProposition2);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵadvance"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵrepeater"](ctx_r1.getPropositions());
  }
}
function AnalysisComponent_Conditional_42_Template(rf, ctx) {
  if (rf & 1) {
    const _r8 = _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵgetCurrentView"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](0, "button", 29);
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵlistener"]("click", function AnalysisComponent_Conditional_42_Template_button_click_0_listener() {
      _angular_core__WEBPACK_IMPORTED_MODULE_20__["ɵɵrestoreView"](_r8);
      const ctx_r1 = _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵnextContext"]();
      return _angular_core__WEBPACK_IMPORTED_MODULE_20__["ɵɵresetView"](ctx_r1.createCTLProperty());
    });
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](1, "mat-icon");
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](2, "autorenew");
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](3, " Create CTL property ");
    _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
  }
}
class AnalysisComponent {
  bpmnModeler;
  snackBar;
  modelCheckingService;
  sharedState;
  cdr;
  // GG generation
  graphGrammarGenerationRunning = false;
  // General BPMN property checking.
  bpmnSpecificPropertiesToBeChecked = [];
  bpmnSpecificVerificationRunning = false;
  bpmnPropertyCheckingResults = [];
  // CTL property checking with templates
  selectedTemplate;
  selectedProposition1 = ''; // We only support one or two propositions.
  selectedProposition2 = '';
  ctlTemplates = [{
    template: proposition => `AG(!${proposition})`,
    description: 'Never reaches',
    twoPropositions: false
  }, {
    template: proposition => `EF(${proposition})`,
    description: 'Can reach',
    twoPropositions: false
  }, {
    template: proposition => `AF(${proposition})`,
    description: 'Always reaches',
    twoPropositions: false
  }, {
    template: (proposition1, proposition2) => `AG(${proposition1} -> AF(${proposition2}))`,
    description: 'Response',
    twoPropositions: true
  }];
  // CTL property checking
  ctlProperty = '';
  ctlPropertyResult;
  constructor(bpmnModeler, snackBar, modelCheckingService, sharedState, cdr) {
    this.bpmnModeler = bpmnModeler;
    this.snackBar = snackBar;
    this.modelCheckingService = modelCheckingService;
    this.sharedState = sharedState;
    this.cdr = cdr;
  }
  downloadGGClicked() {
    var _this = this;
    return (0,_home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__["default"])(function* () {
      _this.graphGrammarGenerationRunning = true;
      const xmlModel = yield _this.bpmnModeler.getBPMNModelXMLBlob();
      _this.modelCheckingService.downloadGG(xmlModel, _this.sharedState.propositions).pipe((0,rxjs__WEBPACK_IMPORTED_MODULE_2__.finalize)(() => _this.graphGrammarGenerationRunning = false)).subscribe({
        error: error => {
          let message = 'An unexpected error occurred.';
          try {
            const errorObject = JSON.parse(new TextDecoder().decode(error.error));
            message = errorObject.message;
          } catch {
            if (error.message) {
              message = error.message;
            }
          }
          console.error(error);
          _this.snackBar.open(message, 'close');
        },
        next: data => {
          // Receive and save as zip.
          const blob = new Blob([data], {
            type: 'application/zip'
          });
          (0,file_saver_es__WEBPACK_IMPORTED_MODULE_18__.saveAs)(blob, _this.sharedState.modelFileName + '.gps.zip');
        }
      });
    })();
  }
  ggInfoClicked() {
    this.snackBar.open('Graph transformation systems are generated for the graph transformation tool Groove. You can find Groove at https://groove.ewi.utwente.nl/.', 'close');
  }
  checkBPMNSpecificPropertiesClicked() {
    var _this2 = this;
    return (0,_home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__["default"])(function* () {
      if (_this2.bpmnSpecificPropertiesToBeChecked.length === 0) {
        _this2.snackBar.open('Please select at least one property for verification.', 'close', {
          duration: 5000
        });
        return;
      }
      _this2.setVerificationRunning(true);
      const xmlModel = yield _this2.bpmnModeler.getBPMNModelXMLBlob();
      _this2.modelCheckingService.checkBPMNSpecificProperties(_this2.bpmnSpecificPropertiesToBeChecked, xmlModel).pipe((0,rxjs__WEBPACK_IMPORTED_MODULE_2__.finalize)(() => _this2.setVerificationRunning(false))).subscribe({
        error: error => {
          console.error(error);
          _this2.snackBar.open(error.error.message, 'close');
          _this2.bpmnPropertyCheckingResults = [];
        },
        next: data => {
          _this2.bpmnPropertyCheckingResults = structuredClone(data.propertyCheckingResults);
          _this2.setProperCompletionHintsIfNeeded();
          _this2.colorDeadActivitiesAndSetNamesIfNeeded();
        }
      });
    })();
  }
  temporalLogicInfoClicked() {
    this.snackBar.openFromComponent(_components_temporal_logic_syntax_temporal_logic_syntax_component__WEBPACK_IMPORTED_MODULE_4__.TemporalLogicSyntaxComponent, {
      duration: 10000
    });
  }
  checkCTLPropertyClicked() {
    var _this3 = this;
    return (0,_home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__["default"])(function* () {
      const xmlModel = yield _this3.bpmnModeler.getBPMNModelXMLBlob();
      _this3.setVerificationRunning(true);
      _this3.modelCheckingService.checkTemporalLogic('CTL', _this3.ctlProperty, xmlModel, _this3.sharedState.propositions).pipe((0,rxjs__WEBPACK_IMPORTED_MODULE_2__.finalize)(() => _this3.setVerificationRunning(false))).subscribe({
        error: error => {
          console.error(error);
          _this3.snackBar.open(error.error.message, 'close');
        },
        next: response => {
          _this3.ctlPropertyResult = response;
        }
      });
    })();
  }
  setProperCompletionHintsIfNeeded() {
    this.bpmnPropertyCheckingResults.forEach(value => {
      if (value.name === 'Proper completion' && value.additionalInfo) {
        const unproperEndEvents = this.getElementsForIDs([value.additionalInfo]);
        this.colorElementsInRed(unproperEndEvents);
        this.setEndNameAsInfo(value, unproperEndEvents);
        void this.bpmnModeler.updateViewerBPMNModel();
      }
    });
  }
  setEndNameAsInfo(value, unproperEndEvents) {
    const flowNodeNameOrIdList = this.getFlowNodeNameOrIdList(unproperEndEvents);
    value.additionalInfo = `The end event ${flowNodeNameOrIdList} consumed more than one token.`;
  }
  colorDeadActivitiesAndSetNamesIfNeeded() {
    this.bpmnPropertyCheckingResults.forEach(value => {
      if (value.name === 'No dead activities' && value.additionalInfo) {
        const deadActivities = this.getElementsForIDs(value.additionalInfo.split(','));
        this.colorElementsInRed(deadActivities);
        this.setActivityNamesAsInfo(value, deadActivities);
        void this.bpmnModeler.updateViewerBPMNModel();
      }
    });
  }
  setActivityNamesAsInfo(value, deadActivities) {
    const deadActivityNames = this.getFlowNodeNameOrIdList(deadActivities);
    if (deadActivities.length > 1) {
      value.additionalInfo = `The dead activities are ${deadActivityNames}.`;
    } else {
      value.additionalInfo = `The dead activity is ${deadActivityNames}.`;
    }
  }
  getFlowNodeNameOrIdList(elements) {
    return elements.map(element => {
      if (!element.businessObject.name) {
        return element.id;
      }
      return element.businessObject.name;
    }).map(name => `"${name}"`).join(', ');
  }
  colorElementsInRed(elementsToColor) {
    const modeling = this.bpmnModeler.getModeler().get('modeling');
    modeling.setColor(elementsToColor, {
      stroke: '#831311',
      fill: '#ffcdd2'
    });
  }
  getElementsForIDs(ids) {
    const elementRegistry = this.bpmnModeler.getModeler().get('elementRegistry');
    return ids.map(id => elementRegistry.get(id)).filter(element => element !== undefined);
  }
  setVerificationRunning(isRunning) {
    this.bpmnSpecificVerificationRunning = isRunning;
    this.cdr.detectChanges();
  }
  getPropositions() {
    return this.sharedState.getPropositionNames();
  }
  getPropositionsNames() {
    return this.getPropositions().join(', ');
  }
  stopEventPropagation($event) {
    // Stops event propagation so steps are not changed while inputting.
    if ($event.key === 'ArrowLeft' || $event.key === 'ArrowRight') {
      $event.stopPropagation();
    }
  }
  createCTLProperty() {
    if (this.selectedTemplate && this.selectedProposition1) {
      this.ctlProperty = this.selectedTemplate.template(this.selectedProposition1, this.selectedProposition2);
    }
  }
  showCreateCTLPropertyButton() {
    if (this.selectedTemplate?.twoPropositions) {
      return this.selectedProposition1.length > 0 && this.selectedProposition2.length > 0;
    }
    return !!this.selectedTemplate && this.selectedProposition1.length > 0;
  }
  static ɵfac = function AnalysisComponent_Factory(__ngFactoryType__) {
    return new (__ngFactoryType__ || AnalysisComponent)(_angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵdirectiveInject"](_services_bpmnmodeler_service__WEBPACK_IMPORTED_MODULE_22__.BPMNModelerService), _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵdirectiveInject"](_angular_material_snack_bar__WEBPACK_IMPORTED_MODULE_5__.MatSnackBar), _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵdirectiveInject"](_services_model_checking_service__WEBPACK_IMPORTED_MODULE_23__.ModelCheckingService), _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵdirectiveInject"](_services_shared_state_service__WEBPACK_IMPORTED_MODULE_24__.SharedStateService), _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵdirectiveInject"](_angular_core__WEBPACK_IMPORTED_MODULE_19__.ChangeDetectorRef));
  };
  static ɵcmp = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵdefineComponent"]({
    type: AnalysisComponent,
    selectors: [["app-analysis"]],
    decls: 64,
    vars: 13,
    consts: [[3, "viewer", "height"], [1, "analysis-buttons"], ["mat-raised-button", "", "color", "primary", "matTooltip", "Download a groove graph transformation system.", "data-testid", "download-gg-btn"], ["mat-icon-button", "", "aria-label", "Graph transformation system download info button", "color", "primary", "matTooltip", "Further graph transformation system information.", "data-testid", "gg-info-btn", 3, "click"], ["appearance", "outlined"], ["animationDuration", "0ms"], ["label", "General BPMN properties"], ["name", "bpmnProperties", "aria-label", "general BPMN Properties", "multiple", "", "data-testid", "bpmn-property-toggles", 1, "margin-top", 3, "ngModelChange", "ngModel"], ["value", "SAFENESS", "matTooltip", "Safeness means that during process execution no more than one token occurs along the same sequence flow.", "matTooltipPosition", "above"], ["value", "OPTION_TO_COMPLETE", "matTooltip", "Option to complete means that any running process instance must eventually complete.", "matTooltipPosition", "above"], ["value", "PROPER_COMPLETION", "matTooltip", "Proper completion means each end event consumes at most one token.", "matTooltipPosition", "above"], ["value", "NO_DEAD_ACTIVITIES", "matTooltip", "Check if any activities will never be executed.", "matTooltipPosition", "above"], ["mat-raised-button", "", "color", "primary", "matTooltip", "Check the selected general BPMN properties using groove.", "data-testid", "check-properties-btn", 1, "margin-top", 3, "click"], ["label", "CTL properties"], [1, "margin-top"], ["name", "ctlTemplate", "data-testid", "ctl-template-select", 3, "ngModelChange", "ngModel"], [3, "value"], [1, "margin-left"], ["mat-raised-button", "", "color", "primary", "matTooltip", "Create a CTL from the chosen template and proposition.", "data-testid", "create-ctl-btn", 1, "margin-left"], [1, "bold"], [1, "form"], ["appearance", "fill", 1, "full-width"], ["matInput", "", "placeholder", "AG(!Unsafe)", "name", "ctlProperty", "data-testid", "ctl-property-input", 3, "ngModelChange", "keydown", "ngModel"], ["mat-raised-button", "", "color", "primary", "matTooltip", "Check the given CTL property using groove.", "data-testid", "check-ctl-btn", 3, "click"], ["mat-icon-button", "", "aria-label", "CTL syntax info button", "color", "primary", "matTooltip", "Show CTL syntax.", 3, "click"], [3, "running", "properties", "ctlPropertyResult"], ["mat-raised-button", "", "color", "primary", "matTooltip", "Download a groove graph transformation system.", "data-testid", "download-gg-btn", 3, "click"], ["name", "propositionForTemplate1", 3, "ngModelChange", "ngModel"], ["name", "propositionForTemplate2", 3, "ngModelChange", "ngModel"], ["mat-raised-button", "", "color", "primary", "matTooltip", "Create a CTL from the chosen template and proposition.", "data-testid", "create-ctl-btn", 1, "margin-left", 3, "click"]],
    template: function AnalysisComponent_Template(rf, ctx) {
      if (rf & 1) {
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelement"](0, "app-diagram", 0);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](1, "div", 1);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵconditionalCreate"](2, AnalysisComponent_Conditional_2_Template, 3, 0, "span")(3, AnalysisComponent_Conditional_3_Template, 4, 0, "button", 2);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](4, "button", 3);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵlistener"]("click", function AnalysisComponent_Template_button_click_4_listener() {
          return ctx.ggInfoClicked();
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](5, "mat-icon");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](6, "info");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]()()();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](7, "mat-card", 4)(8, "mat-card-header")(9, "mat-card-title");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](10, "Analysis");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]()();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](11, "mat-tab-group", 5)(12, "mat-tab", 6)(13, "mat-card-content");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](14, " Select one or more of the following properties to check for the BPMN model. ");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelement"](15, "br");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](16, "mat-button-toggle-group", 7);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtwoWayListener"]("ngModelChange", function AnalysisComponent_Template_mat_button_toggle_group_ngModelChange_16_listener($event) {
          _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtwoWayBindingSet"](ctx.bpmnSpecificPropertiesToBeChecked, $event) || (ctx.bpmnSpecificPropertiesToBeChecked = $event);
          return $event;
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](17, "mat-button-toggle", 8);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](18, " Safeness ");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](19, "mat-button-toggle", 9);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](20, " Option to complete ");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](21, "mat-button-toggle", 10);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](22, " Proper completion ");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](23, "mat-button-toggle", 11);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](24, " No dead activities ");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]()();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelement"](25, "br");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](26, "button", 12);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵlistener"]("click", function AnalysisComponent_Template_button_click_26_listener() {
          return ctx.checkBPMNSpecificPropertiesClicked();
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](27, "mat-icon");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](28, "done_all");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](29, " Check selected properties ");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]()()();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](30, "mat-tab", 13)(31, "mat-card-content")(32, "div");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](33, "Create a CTL property from a template.");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](34, "mat-form-field", 14)(35, "mat-label");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](36, "CTL Template");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](37, "mat-select", 15);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtwoWayListener"]("ngModelChange", function AnalysisComponent_Template_mat_select_ngModelChange_37_listener($event) {
          _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtwoWayBindingSet"](ctx.selectedTemplate, $event) || (ctx.selectedTemplate = $event);
          return $event;
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵrepeaterCreate"](38, AnalysisComponent_For_39_Template, 2, 2, "mat-option", 16, _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵrepeaterTrackByIdentity"]);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]()();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵconditionalCreate"](40, AnalysisComponent_Conditional_40_Template, 6, 2, "mat-form-field", 17);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵconditionalCreate"](41, AnalysisComponent_Conditional_41_Template, 6, 1, "mat-form-field", 17);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵconditionalCreate"](42, AnalysisComponent_Conditional_42_Template, 4, 0, "button", 18);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelement"](43, "mat-divider");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](44, "div", 14);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](45, " Specify a CTL property to check for the BPMN model. ");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](46, "div");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](47, " Your propositions are: ");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](48, "span", 19);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](49);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]()();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](50, "form", 20)(51, "mat-form-field", 21)(52, "mat-label");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](53, "CTL Property");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](54, "textarea", 22);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtwoWayListener"]("ngModelChange", function AnalysisComponent_Template_textarea_ngModelChange_54_listener($event) {
          _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtwoWayBindingSet"](ctx.ctlProperty, $event) || (ctx.ctlProperty = $event);
          return $event;
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵlistener"]("keydown", function AnalysisComponent_Template_textarea_keydown_54_listener($event) {
          return ctx.stopEventPropagation($event);
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]()()();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](55, "div", 1)(56, "button", 23);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵlistener"]("click", function AnalysisComponent_Template_button_click_56_listener() {
          return ctx.checkCTLPropertyClicked();
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](57, "mat-icon");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](58, "check");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](59, " Check CTL property ");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](60, "button", 24);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵlistener"]("click", function AnalysisComponent_Template_button_click_60_listener() {
          return ctx.temporalLogicInfoClicked();
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementStart"](61, "mat-icon");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtext"](62, "info");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelementEnd"]()()()()()()();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵelement"](63, "app-analysis-result", 25);
      }
      if (rf & 2) {
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵproperty"]("viewer", true)("height", "350");
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵadvance"](2);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵconditional"](ctx.graphGrammarGenerationRunning ? 2 : 3);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵadvance"](14);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtwoWayProperty"]("ngModel", ctx.bpmnSpecificPropertiesToBeChecked);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵadvance"](21);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtwoWayProperty"]("ngModel", ctx.selectedTemplate);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵadvance"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵrepeater"](ctx.ctlTemplates);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵadvance"](2);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵconditional"](ctx.selectedTemplate ? 40 : -1);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵadvance"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵconditional"](ctx.selectedTemplate && ctx.selectedTemplate.twoPropositions ? 41 : -1);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵadvance"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵconditional"](ctx.showCreateCTLPropertyButton() ? 42 : -1);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵadvance"](7);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtextInterpolate"](ctx.getPropositionsNames());
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵadvance"](5);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵtwoWayProperty"]("ngModel", ctx.ctlProperty);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵadvance"](9);
        _angular_core__WEBPACK_IMPORTED_MODULE_21__["ɵɵproperty"]("running", ctx.bpmnSpecificVerificationRunning)("properties", ctx.bpmnPropertyCheckingResults)("ctlPropertyResult", ctx.ctlPropertyResult);
      }
    },
    dependencies: [_angular_forms__WEBPACK_IMPORTED_MODULE_1__.FormsModule, _angular_forms__WEBPACK_IMPORTED_MODULE_1__["ɵNgNoValidate"], _angular_forms__WEBPACK_IMPORTED_MODULE_1__.DefaultValueAccessor, _angular_forms__WEBPACK_IMPORTED_MODULE_1__.NgControlStatus, _angular_forms__WEBPACK_IMPORTED_MODULE_1__.NgControlStatusGroup, _angular_forms__WEBPACK_IMPORTED_MODULE_1__.NgModel, _angular_forms__WEBPACK_IMPORTED_MODULE_1__.NgForm, _angular_material_card__WEBPACK_IMPORTED_MODULE_6__.MatCardModule, _angular_material_card__WEBPACK_IMPORTED_MODULE_6__.MatCard, _angular_material_card__WEBPACK_IMPORTED_MODULE_6__.MatCardContent, _angular_material_card__WEBPACK_IMPORTED_MODULE_6__.MatCardHeader, _angular_material_card__WEBPACK_IMPORTED_MODULE_6__.MatCardTitle, _angular_material_button__WEBPACK_IMPORTED_MODULE_7__.MatButtonModule, _angular_material_button__WEBPACK_IMPORTED_MODULE_7__.MatButton, _angular_material_button__WEBPACK_IMPORTED_MODULE_25__.MatIconButton, _angular_material_icon__WEBPACK_IMPORTED_MODULE_8__.MatIconModule, _angular_material_icon__WEBPACK_IMPORTED_MODULE_8__.MatIcon, _angular_material_form_field__WEBPACK_IMPORTED_MODULE_9__.MatFormFieldModule, _angular_material_form_field__WEBPACK_IMPORTED_MODULE_26__.MatFormField, _angular_material_form_field__WEBPACK_IMPORTED_MODULE_26__.MatLabel, _angular_material_input__WEBPACK_IMPORTED_MODULE_10__.MatInputModule, _angular_material_input__WEBPACK_IMPORTED_MODULE_10__.MatInput, _angular_material_select__WEBPACK_IMPORTED_MODULE_11__.MatSelectModule, _angular_material_select__WEBPACK_IMPORTED_MODULE_11__.MatSelect, _angular_material_select__WEBPACK_IMPORTED_MODULE_27__.MatOption, _angular_material_tooltip__WEBPACK_IMPORTED_MODULE_12__.MatTooltipModule, _angular_material_tooltip__WEBPACK_IMPORTED_MODULE_28__.MatTooltip, _angular_material_button_toggle__WEBPACK_IMPORTED_MODULE_13__.MatButtonToggleModule, _angular_material_button_toggle__WEBPACK_IMPORTED_MODULE_13__.MatButtonToggleGroup, _angular_material_button_toggle__WEBPACK_IMPORTED_MODULE_13__.MatButtonToggle, _angular_material_divider__WEBPACK_IMPORTED_MODULE_14__.MatDividerModule, _angular_material_divider__WEBPACK_IMPORTED_MODULE_14__.MatDivider, _angular_material_tabs__WEBPACK_IMPORTED_MODULE_15__.MatTabsModule, _angular_material_tabs__WEBPACK_IMPORTED_MODULE_15__.MatTab, _angular_material_tabs__WEBPACK_IMPORTED_MODULE_15__.MatTabGroup, _angular_material_progress_spinner__WEBPACK_IMPORTED_MODULE_16__.MatProgressSpinnerModule, _angular_material_progress_spinner__WEBPACK_IMPORTED_MODULE_16__.MatProgressSpinner, _angular_material_snack_bar__WEBPACK_IMPORTED_MODULE_5__.MatSnackBarModule, _components_analysis_result_analysis_result_component__WEBPACK_IMPORTED_MODULE_3__.AnalysisResultComponent, _components_diagram_diagram_component__WEBPACK_IMPORTED_MODULE_17__.DiagramComponent],
    styles: [".margin-left[_ngcontent-%COMP%] {\n  margin-left: 10px;\n}\n\n.bold[_ngcontent-%COMP%] {\n  font-weight: bolder;\n}\n\n.form[_ngcontent-%COMP%] {\n  min-width: 150px;\n  max-width: 100%;\n  width: 100%;\n  margin-top: 10px;\n}\n\n.full-width[_ngcontent-%COMP%] {\n  width: 100%;\n}\n\n.analysis-buttons[_ngcontent-%COMP%] {\n  display: flex;\n  justify-content: flex-start;\n  align-items: center;\n}\n/*# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIndlYnBhY2s6Ly8uL3NyYy9hcHAvcGFnZXMvYW5hbHlzaXMvYW5hbHlzaXMuY29tcG9uZW50LnNjc3MiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7RUFDSSxpQkFBQTtBQUNKOztBQUVBO0VBQ0ksbUJBQUE7QUFDSjs7QUFFQTtFQUNJLGdCQUFBO0VBQ0EsZUFBQTtFQUNBLFdBQUE7RUFDQSxnQkFBQTtBQUNKOztBQUVBO0VBQ0ksV0FBQTtBQUNKOztBQUVBO0VBQ0ksYUFBQTtFQUNBLDJCQUFBO0VBQ0EsbUJBQUE7QUFDSiIsInNvdXJjZXNDb250ZW50IjpbIi5tYXJnaW4tbGVmdCB7XG4gICAgbWFyZ2luLWxlZnQ6IDEwcHg7XG59XG5cbi5ib2xkIHtcbiAgICBmb250LXdlaWdodDogYm9sZGVyO1xufVxuXG4uZm9ybSB7XG4gICAgbWluLXdpZHRoOiAxNTBweDtcbiAgICBtYXgtd2lkdGg6IDEwMCU7XG4gICAgd2lkdGg6IDEwMCU7XG4gICAgbWFyZ2luLXRvcDogMTBweDtcbn1cblxuLmZ1bGwtd2lkdGgge1xuICAgIHdpZHRoOiAxMDAlO1xufVxuXG4uYW5hbHlzaXMtYnV0dG9ucyB7XG4gICAgZGlzcGxheTogZmxleDtcbiAgICBqdXN0aWZ5LWNvbnRlbnQ6IGZsZXgtc3RhcnQ7XG4gICAgYWxpZ24taXRlbXM6IGNlbnRlcjtcbn1cbiJdLCJzb3VyY2VSb290IjoiIn0= */"]
  });
}

/***/ },

/***/ 74055
/*!******************************************************!*\
  !*** ./src/app/pages/modeling/modeling.component.ts ***!
  \******************************************************/
(__unused_webpack_module, __webpack_exports__, __webpack_require__) {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   BPMN_FILE_EXTENSION: () => (/* binding */ BPMN_FILE_EXTENSION),
/* harmony export */   ModelingComponent: () => (/* binding */ ModelingComponent)
/* harmony export */ });
/* harmony import */ var _home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./node_modules/@babel/runtime/helpers/esm/asyncToGenerator.js */ 89204);
/* harmony import */ var file_saver_es__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! file-saver-es */ 46244);
/* harmony import */ var _proposition_proposition_component__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../proposition/proposition.component */ 40307);
/* harmony import */ var _angular_material_card__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @angular/material/card */ 53777);
/* harmony import */ var _angular_material_button__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @angular/material/button */ 84175);
/* harmony import */ var _angular_material_icon__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @angular/material/icon */ 93840);
/* harmony import */ var _angular_material_tooltip__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! @angular/material/tooltip */ 80640);
/* harmony import */ var _components_diagram_diagram_component__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! ../../components/diagram/diagram.component */ 76289);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! @angular/core */ 94363);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! @angular/core */ 36124);
/* harmony import */ var _services_bpmnmodeler_service__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! ../../services/bpmnmodeler.service */ 91907);
/* harmony import */ var _services_shared_state_service__WEBPACK_IMPORTED_MODULE_11__ = __webpack_require__(/*! ../../services/shared-state.service */ 42927);
/* harmony import */ var _angular_material_tooltip__WEBPACK_IMPORTED_MODULE_12__ = __webpack_require__(/*! @angular/material/tooltip */ 15575);














const BPMN_FILE_EXTENSION = '.bpmn';
class ModelingComponent {
  bpmnModeler;
  sharedState;
  constructor(bpmnModeler, sharedState) {
    this.bpmnModeler = bpmnModeler;
    this.sharedState = sharedState;
  }
  downloadBPMN() {
    this.bpmnModeler.getBPMNModelXMLBlob().then(result => {
      (0,file_saver_es__WEBPACK_IMPORTED_MODULE_1__.saveAs)(result, this.sharedState.modelFileName + BPMN_FILE_EXTENSION);
    }).catch(error => {
      console.error('Failed to download BPMN model:', error);
    });
  }
  uploadBPMN(event) {
    var _this = this;
    return (0,_home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__["default"])(function* () {
      const file = event.target.files?.[0];
      if (!file) {
        return;
      }
      // Remove file extension: https://stackoverflow.com/questions/4250364/how-to-trim-a-file-extension-from-a-string-in-javascript
      _this.sharedState.modelFileName = file.name.replace(/\.[^/.]+$/, '');
      const fileText = yield file.text();
      yield _this.bpmnModeler.getModeler().importXML(fileText);
    })();
  }
  downloadSVG() {
    this.bpmnModeler.getModeler().saveSVG().then(result => {
      const svgBlob = new Blob([result.svg], {
        type: 'text/plain;charset=utf-8'
      });
      (0,file_saver_es__WEBPACK_IMPORTED_MODULE_1__.saveAs)(svgBlob, this.sharedState.modelFileName + _proposition_proposition_component__WEBPACK_IMPORTED_MODULE_2__.SVG_FILE_EXTENSION);
    }).catch(error => {
      console.error('Failed to download SVG:', error);
    });
  }
  static ɵfac = function ModelingComponent_Factory(__ngFactoryType__) {
    return new (__ngFactoryType__ || ModelingComponent)(_angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵdirectiveInject"](_services_bpmnmodeler_service__WEBPACK_IMPORTED_MODULE_10__.BPMNModelerService), _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵdirectiveInject"](_services_shared_state_service__WEBPACK_IMPORTED_MODULE_11__.SharedStateService));
  };
  static ɵcmp = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵdefineComponent"]({
    type: ModelingComponent,
    selectors: [["app-modeling"]],
    decls: 16,
    vars: 1,
    consts: [["uploader", ""], [3, "propertiesPanel"], [1, "modeler-buttons", "margin-top"], ["mat-raised-button", "", "color", "primary", "matTooltip", "Upload a BPMN model contained in a BPMN-file.", 1, "right-margin", 3, "click"], ["mat-raised-button", "", "matTooltip", "Download the BPMN model as a BPMN-file.", "color", "primary", 1, "right-margin", 3, "click"], ["mat-raised-button", "", "color", "primary", "matTooltip", "Download an SVG of the BPMN token model.", 3, "click"], ["hidden", "", "type", "file", 3, "change"]],
    template: function ModelingComponent_Template(rf, ctx) {
      if (rf & 1) {
        const _r1 = _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵgetCurrentView"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵelement"](0, "app-diagram", 1);
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵelementStart"](1, "div", 2)(2, "button", 3);
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵlistener"]("click", function ModelingComponent_Template_button_click_2_listener() {
          _angular_core__WEBPACK_IMPORTED_MODULE_8__["ɵɵrestoreView"](_r1);
          const uploader_r2 = _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵreference"](15);
          return _angular_core__WEBPACK_IMPORTED_MODULE_8__["ɵɵresetView"](uploader_r2.click());
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵelementStart"](3, "mat-icon");
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵtext"](4, "upload");
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵtext"](5, " Upload model ");
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵelementStart"](6, "button", 4);
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵlistener"]("click", function ModelingComponent_Template_button_click_6_listener() {
          return ctx.downloadBPMN();
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵelementStart"](7, "mat-icon");
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵtext"](8, "download");
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵtext"](9, " Download model ");
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵelementStart"](10, "button", 5);
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵlistener"]("click", function ModelingComponent_Template_button_click_10_listener() {
          return ctx.downloadSVG();
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵelementStart"](11, "mat-icon");
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵtext"](12, "download");
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵtext"](13, " Download SVG ");
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵelementEnd"]()();
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵelementStart"](14, "input", 6, 0);
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵlistener"]("change", function ModelingComponent_Template_input_change_14_listener($event) {
          return ctx.uploadBPMN($event);
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵelementEnd"]();
      }
      if (rf & 2) {
        _angular_core__WEBPACK_IMPORTED_MODULE_9__["ɵɵproperty"]("propertiesPanel", true);
      }
    },
    dependencies: [_angular_material_card__WEBPACK_IMPORTED_MODULE_3__.MatCardModule, _angular_material_button__WEBPACK_IMPORTED_MODULE_4__.MatButtonModule, _angular_material_button__WEBPACK_IMPORTED_MODULE_4__.MatButton, _angular_material_icon__WEBPACK_IMPORTED_MODULE_5__.MatIconModule, _angular_material_icon__WEBPACK_IMPORTED_MODULE_5__.MatIcon, _angular_material_tooltip__WEBPACK_IMPORTED_MODULE_6__.MatTooltipModule, _angular_material_tooltip__WEBPACK_IMPORTED_MODULE_12__.MatTooltip, _components_diagram_diagram_component__WEBPACK_IMPORTED_MODULE_7__.DiagramComponent],
    styles: ["/*# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IiIsInNvdXJjZVJvb3QiOiIifQ== */"]
  });
}

/***/ },

/***/ 40307
/*!************************************************************!*\
  !*** ./src/app/pages/proposition/proposition.component.ts ***!
  \************************************************************/
(__unused_webpack_module, __webpack_exports__, __webpack_require__) {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   PropositionComponent: () => (/* binding */ PropositionComponent),
/* harmony export */   SVG_FILE_EXTENSION: () => (/* binding */ SVG_FILE_EXTENSION)
/* harmony export */ });
/* harmony import */ var _home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./node_modules/@babel/runtime/helpers/esm/asyncToGenerator.js */ 89204);
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/common */ 79748);
/* harmony import */ var file_saver_es__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! file-saver-es */ 46244);
/* harmony import */ var _modeling_modeling_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../modeling/modeling.component */ 74055);
/* harmony import */ var _angular_material_dialog__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @angular/material/dialog */ 12587);
/* harmony import */ var _components_rename_proposition_dialog_rename_proposition_dialog_component__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ../../components/rename-proposition-dialog/rename-proposition-dialog.component */ 32525);
/* harmony import */ var _angular_material_snack_bar__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! @angular/material/snack-bar */ 3347);
/* harmony import */ var _angular_material_card__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! @angular/material/card */ 53777);
/* harmony import */ var _angular_material_button__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! @angular/material/button */ 84175);
/* harmony import */ var _angular_material_icon__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! @angular/material/icon */ 93840);
/* harmony import */ var _angular_material_tooltip__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! @angular/material/tooltip */ 80640);
/* harmony import */ var _angular_material_list__WEBPACK_IMPORTED_MODULE_11__ = __webpack_require__(/*! @angular/material/list */ 20943);
/* harmony import */ var _components_token_diagram_token_diagram_component__WEBPACK_IMPORTED_MODULE_12__ = __webpack_require__(/*! ../../components/token-diagram/token-diagram.component */ 90405);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_13__ = __webpack_require__(/*! @angular/core */ 94363);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_14__ = __webpack_require__(/*! @angular/core */ 36124);
/* harmony import */ var _services_bpmnmodeler_service__WEBPACK_IMPORTED_MODULE_15__ = __webpack_require__(/*! ../../services/bpmnmodeler.service */ 91907);
/* harmony import */ var _services_shared_state_service__WEBPACK_IMPORTED_MODULE_16__ = __webpack_require__(/*! ../../services/shared-state.service */ 42927);
/* harmony import */ var _angular_material_tooltip__WEBPACK_IMPORTED_MODULE_17__ = __webpack_require__(/*! @angular/material/tooltip */ 15575);






















const _c0 = (a0, a1) => ({
  highlight: a0,
  proposition: a1
});
function PropositionComponent_For_7_Template(rf, ctx) {
  if (rf & 1) {
    const _r2 = _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵgetCurrentView"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementStart"](0, "mat-list-item", 12);
    _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵlistener"]("click", function PropositionComponent_For_7_Template_mat_list_item_click_0_listener() {
      const proposition_r3 = _angular_core__WEBPACK_IMPORTED_MODULE_13__["ɵɵrestoreView"](_r2).$implicit;
      const ctx_r3 = _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵnextContext"]();
      return _angular_core__WEBPACK_IMPORTED_MODULE_13__["ɵɵresetView"](ctx_r3.switchToProposition(proposition_r3));
    });
    _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementStart"](1, "mat-icon", 13);
    _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵtext"](2, "description");
    _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementEnd"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementStart"](3, "div", 14);
    _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵtext"](4);
    _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementEnd"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementStart"](5, "mat-icon", 15);
    _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵlistener"]("click", function PropositionComponent_For_7_Template_mat_icon_click_5_listener() {
      const proposition_r3 = _angular_core__WEBPACK_IMPORTED_MODULE_13__["ɵɵrestoreView"](_r2).$implicit;
      const ctx_r3 = _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵnextContext"]();
      return _angular_core__WEBPACK_IMPORTED_MODULE_13__["ɵɵresetView"](ctx_r3.editProposition(proposition_r3));
    });
    _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵtext"](6, "edit ");
    _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementEnd"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementStart"](7, "mat-icon", 16);
    _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵlistener"]("click", function PropositionComponent_For_7_Template_mat_icon_click_7_listener() {
      const proposition_r3 = _angular_core__WEBPACK_IMPORTED_MODULE_13__["ɵɵrestoreView"](_r2).$implicit;
      const ctx_r3 = _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵnextContext"]();
      return _angular_core__WEBPACK_IMPORTED_MODULE_13__["ɵɵresetView"](ctx_r3.deleteProposition(proposition_r3));
    });
    _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵtext"](8, "delete ");
    _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementEnd"]()();
  }
  if (rf & 2) {
    const proposition_r3 = ctx.$implicit;
    const ctx_r3 = _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵnextContext"]();
    _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵproperty"]("ngClass", _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵpureFunction2"](2, _c0, proposition_r3 === ctx_r3.currentProposition, proposition_r3 !== ctx_r3.currentProposition));
    _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵadvance"](4);
    _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵtextInterpolate1"](" ", proposition_r3.name, " ");
  }
}
const SVG_FILE_EXTENSION = '.svg';
class PropositionComponent {
  modeler;
  propService;
  dialog;
  snackBar;
  currentProposition = {
    name: 'Proposition1',
    xml: ''
  };
  constructor(modeler, propService, dialog, snackBar) {
    this.modeler = modeler;
    this.propService = propService;
    this.dialog = dialog;
    this.snackBar = snackBar;
    this.propositions.push(this.currentProposition);
  }
  createNewProposition() {
    var _this = this;
    return (0,_home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__["default"])(function* () {
      const newProposition = {
        name: 'newProposition',
        xml: yield _this.modeler.getBpmnXML()
      };
      _this.propositions.push(newProposition);
      yield _this.switchAndSaveAndLoadXML(newProposition);
    })();
  }
  switchAndSaveAndLoadXML(changeTo) {
    var _this2 = this;
    return (0,_home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__["default"])(function* () {
      if (_this2.currentProposition) {
        _this2.currentProposition.xml = yield _this2.modeler.getTokenXML();
      }
      _this2.currentProposition = changeTo;
      yield _this2.modeler.getTokenModeler().importXML(changeTo.xml);
    })();
  }
  switchToProposition(proposition) {
    var _this3 = this;
    return (0,_home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__["default"])(function* () {
      if (proposition !== _this3.currentProposition) {
        yield _this3.switchAndSaveAndLoadXML(proposition);
      }
    })();
  }
  uploadTokenModel(event) {
    var _this4 = this;
    return (0,_home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__["default"])(function* () {
      const file = event.target.files?.[0];
      if (!file) {
        return;
      }
      const fileText = yield file.text();
      yield _this4.modeler.getTokenModeler().importXML(fileText);
      _this4.currentProposition.xml = fileText;
      if (file.name) {
        // Remove file extension: https://stackoverflow.com/questions/4250364/how-to-trim-a-file-extension-from-a-string-in-javascript
        _this4.currentProposition.name = file.name.replace(/\.[^/.]+$/, '');
      }
    })();
  }
  downloadTokenModel() {
    this.modeler.getTokenModelXMLBlob().then(result => {
      (0,file_saver_es__WEBPACK_IMPORTED_MODULE_2__.saveAs)(result, this.currentProposition.name + _modeling_modeling_component__WEBPACK_IMPORTED_MODULE_3__.BPMN_FILE_EXTENSION);
    }).catch(error => {
      console.error('Failed to download token model:', error);
    });
  }
  get propositions() {
    return this.propService.propositions;
  }
  editProposition(proposition) {
    this.dialog.open(_components_rename_proposition_dialog_rename_proposition_dialog_component__WEBPACK_IMPORTED_MODULE_5__.RenamePropositionDialogComponent, {
      data: {
        proposition
      }
    });
  }
  deleteProposition(prop) {
    var _this5 = this;
    return (0,_home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__["default"])(function* () {
      if (_this5.propositions.length === 1) {
        _this5.snackBar.open('There has to be at least one proposition.', 'close', {
          duration: 5000
        });
        return;
      }
      const index = _this5.propositions.indexOf(prop);
      if (index === -1) {
        return;
      }
      _this5.propositions.splice(index, 1);
      if (prop === _this5.currentProposition) {
        yield _this5.switchToProposition(_this5.propositions[0]);
      }
    })();
  }
  saveCurrentProposition() {
    var _this6 = this;
    return (0,_home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__["default"])(function* () {
      if (_this6.currentProposition.xml === '') {
        yield _this6.modeler.updateTokenBPMNModelIfNeeded();
      }
      _this6.currentProposition.xml = yield _this6.modeler.getTokenXML();
    })();
  }
  downloadTokenModelSVG() {
    this.modeler.getTokenModeler().saveSVG().then(result => {
      const svgBlob = new Blob([result.svg], {
        type: 'text/plain;charset=utf-8'
      });
      (0,file_saver_es__WEBPACK_IMPORTED_MODULE_2__.saveAs)(svgBlob, this.currentProposition.name + SVG_FILE_EXTENSION);
    }).catch(error => {
      console.error('Failed to download token model SVG:', error);
    });
  }
  propositionDown(event) {
    var _this7 = this;
    return (0,_home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__["default"])(function* () {
      if (event.target &&
      // Do not step forward when inputting something in the panel.
      !event.target.classList.contains('bio-properties-panel-input')) {
        const currentIndex = _this7.propositions.findIndex(proposition => proposition === _this7.currentProposition);
        const nextProposition = _this7.propositions[currentIndex + 1];
        if (nextProposition) {
          yield _this7.switchToProposition(nextProposition);
        }
      }
    })();
  }
  propositionUp(event) {
    var _this8 = this;
    return (0,_home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__["default"])(function* () {
      if (event.target &&
      // Do not step forward when inputting something in the panel.
      !event.target.classList.contains('bio-properties-panel-input')) {
        const currentIndex = _this8.propositions.findIndex(proposition => proposition === _this8.currentProposition);
        const nextProposition = _this8.propositions[currentIndex - 1];
        if (nextProposition) {
          yield _this8.switchToProposition(nextProposition);
        }
      }
    })();
  }
  static ɵfac = function PropositionComponent_Factory(__ngFactoryType__) {
    return new (__ngFactoryType__ || PropositionComponent)(_angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵdirectiveInject"](_services_bpmnmodeler_service__WEBPACK_IMPORTED_MODULE_15__.BPMNModelerService), _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵdirectiveInject"](_services_shared_state_service__WEBPACK_IMPORTED_MODULE_16__.SharedStateService), _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵdirectiveInject"](_angular_material_dialog__WEBPACK_IMPORTED_MODULE_4__.MatDialog), _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵdirectiveInject"](_angular_material_snack_bar__WEBPACK_IMPORTED_MODULE_6__.MatSnackBar));
  };
  static ɵcmp = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵdefineComponent"]({
    type: PropositionComponent,
    selectors: [["app-proposition"]],
    hostBindings: function PropositionComponent_HostBindings(rf, ctx) {
      if (rf & 1) {
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵlistener"]("keydown.ArrowDown", function PropositionComponent_keydown_ArrowDown_HostBindingHandler($event) {
          return ctx.propositionDown($event);
        }, _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵresolveDocument"])("keydown.ArrowUp", function PropositionComponent_keydown_ArrowUp_HostBindingHandler($event) {
          return ctx.propositionUp($event);
        }, _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵresolveDocument"]);
      }
    },
    decls: 28,
    vars: 0,
    consts: [["uploader", ""], [1, "propositions-container"], [1, "diagram-parent"], [1, "propositions"], ["mat-subheader", ""], [3, "ngClass"], ["mat-raised-button", "", "color", "primary", "matTooltip", "Create a new proposition", 3, "click"], [1, "modeler-buttons", "margin-top"], ["mat-raised-button", "", "color", "primary", "matTooltip", "Upload the BPMN token model.", 1, "right-margin", 3, "click"], ["mat-raised-button", "", "color", "primary", "matTooltip", "Download the BPMN token model.", 1, "right-margin", 3, "click"], ["mat-raised-button", "", "color", "primary", "matTooltip", "Download an SVG of the BPMN token model.", 3, "click"], ["hidden", "", "type", "file", 3, "change"], [3, "click", "ngClass"], ["matListItemIcon", ""], ["matListItemTitle", ""], ["matListItemIcon", "", "matTooltip", "Edit proposition name", 1, "order", "editButton", "edit-icon-margin", 3, "click"], ["matListItemIcon", "", "matTooltip", "Delete proposition", 1, "order", "editButton", "delete-icon-margin", 3, "click"]],
    template: function PropositionComponent_Template(rf, ctx) {
      if (rf & 1) {
        const _r1 = _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵgetCurrentView"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementStart"](0, "div", 1)(1, "div", 2);
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelement"](2, "app-token-diagram");
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementStart"](3, "mat-list", 3)(4, "div", 4);
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵtext"](5, "Propositions");
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵrepeaterCreate"](6, PropositionComponent_For_7_Template, 9, 5, "mat-list-item", 5, _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵrepeaterTrackByIdentity"]);
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementStart"](8, "mat-list-item")(9, "button", 6);
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵlistener"]("click", function PropositionComponent_Template_button_click_9_listener() {
          return ctx.createNewProposition();
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementStart"](10, "mat-icon");
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵtext"](11, "add");
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵtext"](12, " New proposition ");
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementEnd"]()()()();
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementStart"](13, "div", 7)(14, "button", 8);
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵlistener"]("click", function PropositionComponent_Template_button_click_14_listener() {
          _angular_core__WEBPACK_IMPORTED_MODULE_13__["ɵɵrestoreView"](_r1);
          const uploader_r5 = _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵreference"](27);
          return _angular_core__WEBPACK_IMPORTED_MODULE_13__["ɵɵresetView"](uploader_r5.click());
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementStart"](15, "mat-icon");
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵtext"](16, "upload");
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵtext"](17, " Upload model ");
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementStart"](18, "button", 9);
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵlistener"]("click", function PropositionComponent_Template_button_click_18_listener() {
          return ctx.downloadTokenModel();
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementStart"](19, "mat-icon");
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵtext"](20, "download");
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵtext"](21, " Download model ");
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementStart"](22, "button", 10);
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵlistener"]("click", function PropositionComponent_Template_button_click_22_listener() {
          return ctx.downloadTokenModelSVG();
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementStart"](23, "mat-icon");
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵtext"](24, "download");
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementEnd"]();
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵtext"](25, " Download SVG ");
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementEnd"]()();
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementStart"](26, "input", 11, 0);
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵlistener"]("change", function PropositionComponent_Template_input_change_26_listener($event) {
          return ctx.uploadTokenModel($event);
        });
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵelementEnd"]();
      }
      if (rf & 2) {
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵadvance"](6);
        _angular_core__WEBPACK_IMPORTED_MODULE_14__["ɵɵrepeater"](ctx.propositions);
      }
    },
    dependencies: [_angular_common__WEBPACK_IMPORTED_MODULE_1__.NgClass, _angular_material_card__WEBPACK_IMPORTED_MODULE_7__.MatCardModule, _angular_material_button__WEBPACK_IMPORTED_MODULE_8__.MatButtonModule, _angular_material_button__WEBPACK_IMPORTED_MODULE_8__.MatButton, _angular_material_icon__WEBPACK_IMPORTED_MODULE_9__.MatIconModule, _angular_material_icon__WEBPACK_IMPORTED_MODULE_9__.MatIcon, _angular_material_tooltip__WEBPACK_IMPORTED_MODULE_10__.MatTooltipModule, _angular_material_tooltip__WEBPACK_IMPORTED_MODULE_17__.MatTooltip, _angular_material_list__WEBPACK_IMPORTED_MODULE_11__.MatListModule, _angular_material_list__WEBPACK_IMPORTED_MODULE_11__.MatList, _angular_material_list__WEBPACK_IMPORTED_MODULE_11__.MatListItem, _angular_material_list__WEBPACK_IMPORTED_MODULE_11__.MatListItemIcon, _angular_material_list__WEBPACK_IMPORTED_MODULE_11__.MatListSubheaderCssMatStyler, _angular_material_list__WEBPACK_IMPORTED_MODULE_11__.MatListItemTitle, _angular_material_dialog__WEBPACK_IMPORTED_MODULE_4__.MatDialogModule, _angular_material_snack_bar__WEBPACK_IMPORTED_MODULE_6__.MatSnackBarModule, _components_token_diagram_token_diagram_component__WEBPACK_IMPORTED_MODULE_12__.TokenDiagramComponent],
    styles: [".proposition[_ngcontent-%COMP%]:hover {\n  background: rgba(0, 0, 0, 0.04);\n}\n\n.dark-theme[_nghost-%COMP%]   .proposition[_ngcontent-%COMP%]:hover, .dark-theme   [_nghost-%COMP%]   .proposition[_ngcontent-%COMP%]:hover {\n  background: rgba(255, 255, 255, 0.08);\n}\n\n.highlight[_ngcontent-%COMP%] {\n  background: #dddddd;\n}\n\n.dark-theme[_nghost-%COMP%]   .highlight[_ngcontent-%COMP%], .dark-theme   [_nghost-%COMP%]   .highlight[_ngcontent-%COMP%] {\n  background: #444444;\n}\n\n.diagram-parent[_ngcontent-%COMP%] {\n  height: 750px;\n  border: solid 3px #eee;\n  position: relative;\n  resize: both;\n  overflow: auto;\n  max-height: 100%;\n  width: 80%;\n  max-width: 90%;\n}\n\n.dark-theme[_nghost-%COMP%]   .diagram-parent[_ngcontent-%COMP%], .dark-theme   [_nghost-%COMP%]   .diagram-parent[_ngcontent-%COMP%] {\n  border-color: #555555;\n}\n\n.propositions-container[_ngcontent-%COMP%] {\n  display: flex;\n  justify-content: flex-start;\n  align-items: flex-start;\n}\n\n.propositions[_ngcontent-%COMP%] {\n  width: 20%;\n  max-width: 20%;\n}\n\n.order[_ngcontent-%COMP%] {\n  order: 1;\n}\n\n.editButton[_ngcontent-%COMP%]:hover {\n  cursor: pointer;\n}\n\n.edit-icon-margin[_ngcontent-%COMP%] {\n  margin-right: -1px;\n}\n\n.delete-icon-margin[_ngcontent-%COMP%] {\n  margin-right: -1px;\n}\n/*# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIndlYnBhY2s6Ly8uL3NyYy9hcHAvcGFnZXMvcHJvcG9zaXRpb24vcHJvcG9zaXRpb24uY29tcG9uZW50LnNjc3MiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7RUFDSSwrQkFBQTtBQUNKOztBQUVBO0VBQ0kscUNBQUE7QUFDSjs7QUFFQTtFQUNJLG1CQUFBO0FBQ0o7O0FBRUE7RUFDSSxtQkFBQTtBQUNKOztBQUVBO0VBQ0ksYUFBQTtFQUNBLHNCQUFBO0VBQ0Esa0JBQUE7RUFDQSxZQUFBO0VBQ0EsY0FBQTtFQUNBLGdCQUFBO0VBQ0EsVUFBQTtFQUNBLGNBQUE7QUFDSjs7QUFFQTtFQUNJLHFCQUFBO0FBQ0o7O0FBRUE7RUFDSSxhQUFBO0VBQ0EsMkJBQUE7RUFDQSx1QkFBQTtBQUNKOztBQUVBO0VBQ0ksVUFBQTtFQUNBLGNBQUE7QUFDSjs7QUFFQTtFQUNJLFFBQUE7QUFDSjs7QUFFQTtFQUNJLGVBQUE7QUFDSjs7QUFFQTtFQUNJLGtCQUFBO0FBQ0o7O0FBRUE7RUFDSSxrQkFBQTtBQUNKIiwic291cmNlc0NvbnRlbnQiOlsiLnByb3Bvc2l0aW9uOmhvdmVyIHtcbiAgICBiYWNrZ3JvdW5kOiByZ2JhKDAsIDAsIDAsIDAuMDQpO1xufVxuXG46aG9zdC1jb250ZXh0KC5kYXJrLXRoZW1lKSAucHJvcG9zaXRpb246aG92ZXIge1xuICAgIGJhY2tncm91bmQ6IHJnYmEoMjU1LCAyNTUsIDI1NSwgMC4wOCk7XG59XG5cbi5oaWdobGlnaHQge1xuICAgIGJhY2tncm91bmQ6ICNkZGRkZGQ7XG59XG5cbjpob3N0LWNvbnRleHQoLmRhcmstdGhlbWUpIC5oaWdobGlnaHQge1xuICAgIGJhY2tncm91bmQ6ICM0NDQ0NDQ7XG59XG5cbi5kaWFncmFtLXBhcmVudCB7XG4gICAgaGVpZ2h0OiA3NTBweDtcbiAgICBib3JkZXI6IHNvbGlkIDNweCAjZWVlO1xuICAgIHBvc2l0aW9uOiByZWxhdGl2ZTtcbiAgICByZXNpemU6IGJvdGg7XG4gICAgb3ZlcmZsb3c6IGF1dG87XG4gICAgbWF4LWhlaWdodDogMTAwJTtcbiAgICB3aWR0aDogODAlO1xuICAgIG1heC13aWR0aDogOTAlO1xufVxuXG46aG9zdC1jb250ZXh0KC5kYXJrLXRoZW1lKSAuZGlhZ3JhbS1wYXJlbnQge1xuICAgIGJvcmRlci1jb2xvcjogIzU1NTU1NTtcbn1cblxuLnByb3Bvc2l0aW9ucy1jb250YWluZXIge1xuICAgIGRpc3BsYXk6IGZsZXg7XG4gICAganVzdGlmeS1jb250ZW50OiBmbGV4LXN0YXJ0O1xuICAgIGFsaWduLWl0ZW1zOiBmbGV4LXN0YXJ0O1xufVxuXG4ucHJvcG9zaXRpb25zIHtcbiAgICB3aWR0aDogMjAlO1xuICAgIG1heC13aWR0aDogMjAlO1xufVxuXG4ub3JkZXIge1xuICAgIG9yZGVyOiAxO1xufVxuXG4uZWRpdEJ1dHRvbjpob3ZlciB7XG4gICAgY3Vyc29yOiBwb2ludGVyO1xufVxuXG4uZWRpdC1pY29uLW1hcmdpbiB7XG4gICAgbWFyZ2luLXJpZ2h0OiAtMXB4O1xufVxuXG4uZGVsZXRlLWljb24tbWFyZ2luIHtcbiAgICBtYXJnaW4tcmlnaHQ6IC0xcHg7XG59XG4iXSwic291cmNlUm9vdCI6IiJ9 */"]
  });
}

/***/ },

/***/ 91907
/*!*************************************************!*\
  !*** ./src/app/services/bpmnmodeler.service.ts ***!
  \*************************************************/
(__unused_webpack_module, __webpack_exports__, __webpack_require__) {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   BPMNModelerService: () => (/* binding */ BPMNModelerService)
/* harmony export */ });
/* harmony import */ var _home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./node_modules/@babel/runtime/helpers/esm/asyncToGenerator.js */ 89204);
/* harmony import */ var bpmn_js_lib_Modeler__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! bpmn-js/lib/Modeler */ 93370);
/* harmony import */ var bpmn_token_lib_Modeler__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! bpmn-token/lib/Modeler */ 86042);
/* harmony import */ var bpmn_js_lib_Viewer__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! bpmn-js/lib/Viewer */ 2424);
/* harmony import */ var bpmn_token_lib_features_token_context_pad_TokenContextPadProvider__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! bpmn-token/lib/features/token-context-pad/TokenContextPadProvider */ 63354);
/* harmony import */ var bpmn_token_lib_features_token_palette_TokenPaletteProvider__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! bpmn-token/lib/features/token-palette/TokenPaletteProvider */ 62233);
/* harmony import */ var bpmn_token_lib_features_token_keyboard_TokenKeyboardBindings__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! bpmn-token/lib/features/token-keyboard/TokenKeyboardBindings */ 42056);
/* harmony import */ var bpmn_token_lib_features_token_rules_TokenRules__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! bpmn-token/lib/features/token-rules/TokenRules */ 17044);
/* harmony import */ var diagram_js_lib_navigation_keyboard_move__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! diagram-js/lib/navigation/keyboard-move */ 75891);
/* harmony import */ var diagram_js_lib_navigation_movecanvas__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! diagram-js/lib/navigation/movecanvas */ 7203);
/* harmony import */ var diagram_js_lib_navigation_zoomscroll__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! diagram-js/lib/navigation/zoomscroll */ 74124);
/* harmony import */ var bpmn_js_properties_panel__WEBPACK_IMPORTED_MODULE_11__ = __webpack_require__(/*! bpmn-js-properties-panel */ 47147);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_12__ = __webpack_require__(/*! @angular/core */ 94363);













const tokenOverrideModule = {
  contextPadProvider: ['type', bpmn_token_lib_features_token_context_pad_TokenContextPadProvider__WEBPACK_IMPORTED_MODULE_4__["default"]],
  paletteProvider: ['type', bpmn_token_lib_features_token_palette_TokenPaletteProvider__WEBPACK_IMPORTED_MODULE_5__["default"]],
  keyboardBindings: ['type', bpmn_token_lib_features_token_keyboard_TokenKeyboardBindings__WEBPACK_IMPORTED_MODULE_6__["default"]],
  bpmnRules: ['type', bpmn_token_lib_features_token_rules_TokenRules__WEBPACK_IMPORTED_MODULE_7__["default"]]
};
class BPMNModelerService {
  modeler = new bpmn_js_lib_Modeler__WEBPACK_IMPORTED_MODULE_1__["default"]({
    additionalModules: [bpmn_js_properties_panel__WEBPACK_IMPORTED_MODULE_11__.BpmnPropertiesPanelModule, bpmn_js_properties_panel__WEBPACK_IMPORTED_MODULE_11__.BpmnPropertiesProviderModule]
  });
  tokenModeler = new bpmn_token_lib_Modeler__WEBPACK_IMPORTED_MODULE_2__["default"]({
    additionalModules: [tokenOverrideModule]
  });
  viewer = new bpmn_js_lib_Viewer__WEBPACK_IMPORTED_MODULE_3__["default"]({
    additionalModules: [diagram_js_lib_navigation_keyboard_move__WEBPACK_IMPORTED_MODULE_8__["default"], diagram_js_lib_navigation_movecanvas__WEBPACK_IMPORTED_MODULE_9__["default"], diagram_js_lib_navigation_zoomscroll__WEBPACK_IMPORTED_MODULE_10__["default"]]
  });
  lastXMLLoadedByTokenModeler = '';
  getModeler() {
    return this.modeler;
  }
  getViewer() {
    return this.viewer;
  }
  getTokenModeler() {
    return this.tokenModeler;
  }
  getBPMNModelXMLBlob() {
    var _this = this;
    return (0,_home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__["default"])(function* () {
      const xmlResult = yield _this.getModeler().saveXML({
        format: true
      });
      return _this.returnAsBlob(xmlResult);
    })();
  }
  getTokenModelXMLBlob() {
    var _this2 = this;
    return (0,_home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__["default"])(function* () {
      const xmlResult = yield _this2.getTokenModeler().saveXML({
        format: true
      });
      return _this2.returnAsBlob(xmlResult);
    })();
  }
  returnAsBlob(xmlResult) {
    if (xmlResult.xml) {
      return new Blob([xmlResult.xml], {
        type: 'text/xml;charset=utf-8'
      });
    }
    return new Blob([]);
  }
  /**
   * Updates the Viewer with the newest bpmn model from the modeler.
   */
  updateViewerBPMNModel() {
    var _this3 = this;
    return (0,_home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__["default"])(function* () {
      const saveXMLResult = yield _this3.modeler.saveXML();
      if (saveXMLResult.xml) {
        yield _this3.viewer.importXML(saveXMLResult.xml);
      }
    })();
  }
  updateTokenBPMNModelIfNeeded() {
    var _this4 = this;
    return (0,_home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__["default"])(function* () {
      const saveXMLResult = yield _this4.modeler.saveXML();
      if (saveXMLResult.xml && _this4.lastXMLLoadedByTokenModeler !== saveXMLResult.xml) {
        yield _this4.tokenModeler.importXML(saveXMLResult.xml);
        _this4.lastXMLLoadedByTokenModeler = saveXMLResult.xml;
      }
    })();
  }
  getBpmnXML() {
    var _this5 = this;
    return (0,_home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__["default"])(function* () {
      return _this5.getXML(_this5.modeler);
    })();
  }
  getTokenXML() {
    var _this6 = this;
    return (0,_home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__["default"])(function* () {
      return _this6.getXML(_this6.tokenModeler);
    })();
  }
  getXML(modeler) {
    return (0,_home_runner_work_BPMN_Analyzer_BPMN_Analyzer_generation_ui_node_modules_babel_runtime_helpers_esm_asyncToGenerator_js__WEBPACK_IMPORTED_MODULE_0__["default"])(function* () {
      const saveXMLResult = yield modeler.saveXML();
      if (saveXMLResult.xml) {
        return saveXMLResult.xml;
      }
      return '';
    })();
  }
  static ɵfac = function BPMNModelerService_Factory(__ngFactoryType__) {
    return new (__ngFactoryType__ || BPMNModelerService)();
  };
  static ɵprov = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_12__["ɵɵdefineInjectable"]({
    token: BPMNModelerService,
    factory: BPMNModelerService.ɵfac,
    providedIn: 'root'
  });
}

/***/ },

/***/ 15482
/*!****************************************************!*\
  !*** ./src/app/services/model-checking.service.ts ***!
  \****************************************************/
(__unused_webpack_module, __webpack_exports__, __webpack_require__) {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   ModelCheckingService: () => (/* binding */ ModelCheckingService)
/* harmony export */ });
/* harmony import */ var _environments_environment__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../environments/environment */ 45312);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ 94363);
/* harmony import */ var _angular_common_http__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common/http */ 50698);



const baseURL = _environments_environment__WEBPACK_IMPORTED_MODULE_0__.environment.production ? globalThis.location.href : _environments_environment__WEBPACK_IMPORTED_MODULE_0__.environment.apiURL;
const generateGGAndZipURL = baseURL + 'generateGGAndZip';
const checkBPMNSpecificPropertiesURL = baseURL + 'checkBPMNSpecificProperties';
const checkTemporalLogicPropertyURL = baseURL + 'checkTemporalLogic';
class ModelCheckingService {
  httpClient;
  constructor(httpClient) {
    this.httpClient = httpClient;
  }
  downloadGG(xmlModel, propositions) {
    const formData = new FormData();
    formData.append('file', xmlModel);
    formData.append('propositions', JSON.stringify(propositions));
    return this.httpClient.post(generateGGAndZipURL, formData, {
      responseType: 'arraybuffer'
    });
  }
  checkBPMNSpecificProperties(bpmnSpecificPropertiesToBeChecked, xmlModel) {
    const formData = new FormData();
    bpmnSpecificPropertiesToBeChecked.forEach(property => formData.append('propertiesToBeChecked', property));
    formData.append('file', xmlModel);
    return this.httpClient.post(checkBPMNSpecificPropertiesURL, formData);
  }
  checkTemporalLogic(logic, property, xmlModel, propositions = []) {
    const formData = new FormData();
    formData.append('logic', logic);
    formData.append('property', property);
    formData.append('file', xmlModel);
    formData.append('propositions', JSON.stringify(propositions));
    return this.httpClient.post(checkTemporalLogicPropertyURL, formData);
  }
  static ɵfac = function ModelCheckingService_Factory(__ngFactoryType__) {
    return new (__ngFactoryType__ || ModelCheckingService)(_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵinject"](_angular_common_http__WEBPACK_IMPORTED_MODULE_2__.HttpClient));
  };
  static ɵprov = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineInjectable"]({
    token: ModelCheckingService,
    factory: ModelCheckingService.ɵfac,
    providedIn: 'root'
  });
}

/***/ },

/***/ 42927
/*!**************************************************!*\
  !*** ./src/app/services/shared-state.service.ts ***!
  \**************************************************/
(__unused_webpack_module, __webpack_exports__, __webpack_require__) {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   SharedStateService: () => (/* binding */ SharedStateService)
/* harmony export */ });
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/core */ 94363);

class SharedStateService {
  modelFileName = 'model';
  propositions = [];
  getPropositionNames() {
    return this.propositions.map(proposition => proposition.name);
  }
  static ɵfac = function SharedStateService_Factory(__ngFactoryType__) {
    return new (__ngFactoryType__ || SharedStateService)();
  };
  static ɵprov = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdefineInjectable"]({
    token: SharedStateService,
    factory: SharedStateService.ɵfac,
    providedIn: 'root'
  });
}

/***/ },

/***/ 70487
/*!*******************************************!*\
  !*** ./src/app/services/theme.service.ts ***!
  \*******************************************/
(__unused_webpack_module, __webpack_exports__, __webpack_require__) {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   ThemeService: () => (/* binding */ ThemeService)
/* harmony export */ });
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/core */ 94363);

class ThemeService {
  storageKey = 'dark-theme';
  _isDarkMode = false;
  get isDarkMode() {
    return this._isDarkMode;
  }
  constructor() {
    const stored = localStorage.getItem(this.storageKey);
    if (stored !== null) {
      this._isDarkMode = stored === 'true';
    } else {
      this._isDarkMode = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    }
    this.applyTheme();
  }
  toggle() {
    this._isDarkMode = !this._isDarkMode;
    localStorage.setItem(this.storageKey, String(this._isDarkMode));
    this.applyTheme();
  }
  applyTheme() {
    if (this._isDarkMode) {
      document.documentElement.classList.add('dark-theme');
    } else {
      document.documentElement.classList.remove('dark-theme');
    }
  }
  static ɵfac = function ThemeService_Factory(__ngFactoryType__) {
    return new (__ngFactoryType__ || ThemeService)();
  };
  static ɵprov = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_0__["ɵɵdefineInjectable"]({
    token: ThemeService,
    factory: ThemeService.ɵfac,
    providedIn: 'root'
  });
}

/***/ },

/***/ 45312
/*!*****************************************!*\
  !*** ./src/environments/environment.ts ***!
  \*****************************************/
(__unused_webpack_module, __webpack_exports__, __webpack_require__) {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   environment: () => (/* binding */ environment)
/* harmony export */ });
// This file can be replaced during build by using the `fileReplacements` array.
// `ng build` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.
const environment = {
  production: false,
  apiURL: 'http://localhost:8080/'
};

/***/ },

/***/ 84429
/*!*********************!*\
  !*** ./src/main.ts ***!
  \*********************/
(__unused_webpack_module, __webpack_exports__, __webpack_require__) {

__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/core */ 37580);
/* harmony import */ var _angular_platform_browser__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/platform-browser */ 94967);
/* harmony import */ var _angular_platform_browser_animations__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/platform-browser/animations */ 43835);
/* harmony import */ var _angular_common_http__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @angular/common/http */ 50698);
/* harmony import */ var _angular_cdk_stepper__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @angular/cdk/stepper */ 63985);
/* harmony import */ var _app_app_component__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ./app/app.component */ 20092);
/* harmony import */ var _environments_environment__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ./environments/environment */ 45312);







if (_environments_environment__WEBPACK_IMPORTED_MODULE_6__.environment.production) {
  (0,_angular_core__WEBPACK_IMPORTED_MODULE_0__.enableProdMode)();
}
(0,_angular_platform_browser__WEBPACK_IMPORTED_MODULE_1__.bootstrapApplication)(_app_app_component__WEBPACK_IMPORTED_MODULE_5__.AppComponent, {
  providers: [(0,_angular_platform_browser_animations__WEBPACK_IMPORTED_MODULE_2__.provideAnimations)(), (0,_angular_common_http__WEBPACK_IMPORTED_MODULE_3__.provideHttpClient)(), {
    provide: _angular_cdk_stepper__WEBPACK_IMPORTED_MODULE_4__.STEPPER_GLOBAL_OPTIONS,
    useValue: {
      displayDefaultIndicatorType: false
    }
  }]
}).catch(err => console.error(err));

/***/ }

},
/******/ __webpack_require__ => { // webpackRuntimeModules
/******/ var __webpack_exec__ = (moduleId) => (__webpack_require__(__webpack_require__.s = moduleId))
/******/ __webpack_require__.O(0, ["vendor"], () => (__webpack_exec__(84429)));
/******/ var __webpack_exports__ = __webpack_require__.O();
/******/ }
]);
//# sourceMappingURL=main.js.map