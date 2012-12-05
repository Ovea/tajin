(function ($) {
    var logger = new Logger('zimmer.template');

    function add(name, html) {
        zimmer.template[name] = {
            merge:function (obj) {
                logger.debug('Merging template: ' + name);
                return $($.nano(html, obj || null));
            }
        }
    }

    zimmer.template = {
        options:function (element, items) {
            var template_name;
            if (element.prop('tagName') != 'SELECT') {
                throw new Error('Options can only be processed on a select element');
            }
            element.empty();
            var list = [];
            if (items && items.length) {
                if (items[0].hasOwnProperty('code') && items[0].hasOwnProperty('name')) {
                    template_name = 'option_code_name';
                    list = items;
                } else if (items[0].hasOwnProperty('key') && items[0].hasOwnProperty('value')) {
                    template_name = 'option_key_value';
                    list = items;
                }
            }
            if (template_name) {
                // if only one choice, remove please select...
                if (list.length > 0 && list.length <= 2 && (list[0].code === '' || list[0].type === '')) {
                    list.shift();
                }
                for (var i = 0; i < list.length; i++) {
                    element.append(zimmer.template[template_name].merge(list[i]));
                }
                element.find("option:first").attr("selected", "selected");
                if(list.length == 1) {
                    element.attr('disabled', 'disabled');
                } else {
                    element.removeAttr('disabled');
                }
                i18n.localize(element);
            }
        },
        errors:function (el, ns, errors) {
            ns = typeof  ns === 'string' ? [ns] : ns;
            el = $(el);
            for (var prop in errors) {
                var field = el.find('[name=' + prop + ']');
                field.parents('.control-group').addClass('error');
                var msg = '';
                for (var i = 0; (!msg || msg.charAt(0) === '[') && i < ns.length; i++) {
                    msg = i18n.message(ns[i] + '.' + prop + '.' + errors[prop]);
                }
                field.parent().append(zimmer.template['field_error'].merge({
                    message: msg
                }));
            }
        }
    };

    $.fn.templateErrors = function (ns, errors) {
        zimmer.template.errors($(this), ns, errors);
        return this;
    };

    $.fn.templateOptions = function (items) {
        if($(this).length) {
            zimmer.template.options($(this), items);
        }
        return this;
    };

    add('option_code_name', '<option value="{code}">{name}</option>');
    add('option_key_value', '<option value="{key}">{value}</option>');
    add('option_key', '<option value="{key}" rel="localize[keys.{key}]"></option>');
    add('field_error', '<span class="help-inline pull-right">{message}</span>');

    add('group_type_row', '<tr data-type="{type}"><td><div class="folder-icon">{name}</div></td><td>{desc}</td><td>{count}</td></tr>');
    add('group_row', '<tr data-type="{id}"><td><div class="group-icon">{name}</div></td><td>{supervisor.fullname}</td><td>{leader.fullname}</td><td>{street}</td><td>{city}</td><td>{statename}</td><td>{countryname}</td><td><i/>&nbsp;</td></tr>');

    add('team_info', '<div><div class="clearfix"><h2 class="pull-left">{name}</h2><button class="edit pull-right btn btn-mini btn-success" data-target="#group_edit_modal" data-toggle="modal" rel="localize[label.edit]"></button></div><div><span rel="localize[team.label.type]"></span>: <span>{typename}</span></div><div><span>{street}</span><br/><span>{city} {statename}</span><br/><span>{zip}</span><br/><span>{countryname}</span></div></div>');
    add('team_supervisor', '<div><div class="clearfix"><h2 rel="localize[team.label.supervisor]" class="pull-left"></h2><button class="edit pull-right btn btn-mini btn-success" rel="localize[label.edit]"></button></div><a href="#" supervisor-id="{id}">{fullname}</a><br/><div><span>{rolename}</span></div><div><span>{group.name}</span></div><div><a href="mailto:{email}">{email}</a></div><div><span>{phone}</span></div></div>');
    add('team_leader', '<div><div class="clearfix"><h2 rel="localize[team.label.leader]" class="pull-left"></h2><button class="edit pull-right btn btn-mini btn-success" rel="localize[label.edit]"></button></div><a href="#" leader-id="{id}">{fullname}</a><br/><div><span>{rolename}</span></div><div><a href="mailto:{email}">{email}</a></div><div><span>{phone}</span></div></div>');
    add('team_user_row', '<tr user-id="{id}"><td>{fullname}</td><td>{rolename}</td><td><a href="mailto:{email}">{email}</a></td><td>{statusname}</td><td>&nbsp;</td></tr>');
    add('team_user_row_actions', '<a style="display: block;" href="{action}"><i class="{icon} left"></i><span rel="localize[team.label.{action}]"></span></a>');

    add('profile_info', '<div style="position: relative;"><div class="clearfix"><h2 class="pull-left">{fullname}</h2><button class="edit pull-right btn btn-mini btn-success" rel="localize[label.edit]" data-target="#profile_edit_modal" data-toggle="modal"></button></div><div><span>{role}</span></div><div><a href="mailto:{email}"><span>{email}</span></a></div><div><span>{phone}</span></div><div><span>{country} ({lang})</span></div><div style="position: absolute; right: 0;bottom: 0;"><button class="btn btn-mini btn-success change-password" rel="localize[profile.label.editpass]" data-target="#password_edit_modal" data-toggle="modal"></button></div></div>');
    add('profile_group_info', '<div><div class="clearfix"><h2 class="pull-left" rel="localize[profile.label.group.title]"></h2><button class="view pull-right btn btn-mini btn-success" rel="localize[profile.label.view]"></button></div><div><span>{group.name}</span></div><div><span rel="localize[profile.label.type]"></span>: <span>{grouptype}</span></div><br/><div><strong rel="localize[profile.label.leader]"></strong>:</div><div><span>{group.leader.fullname}</span></div><div><a href="mailto:{group.leader.email}"><span>{group.leader.email}</span></a></div><div><span>{group.leader.phone}</span></div></div>');

    add('case_row', '<tr data-id="{id}" ><td><div>{referenceId}</div></td><td>{patient.fullname}</td><td>{i18n_status}</td><td>{procedure_name}</td><td>{fmt_surgery_date}</td></tr>');
    add('case_row_PURCHASING', '<tr data-id="{id}"><td><div>{referenceId}</div></td><td>{i18n_status}</td><td>{procedure_name}</td><td>{fmt_surgery_date}</td><td>{fmt_po_date}</td><td class="po_num"><span>{purchase_order}</span></td></tr>');
    add('case_row_SHIPPING', '<tr data-id="{id}"><td><div>{referenceId}</div></td><td>{i18n_status}</td><td>{procedure_name}</td><td>{fmt_surgery_date}</td><td>{fmt_po_date}</td><td class="po_num"><span>{purchase_order}</span></td><td>&nbsp;</td></tr>');
    add('case_row_actions', '<button status="{status}" class="btn btn-mini btn-success"><i class="{icon} left"></i><span>{label}</span></button>');

    add('case_bone_implant', '<div class="group-info"><div class="control-group"><label class="control-label" rel="localize[cases.form.label.{value}_implant]"></label><div class="controls"><select data-type="family" name="implant_family_{code}" class="id name"></select><i class="icon-arrow-right"></i><select data-type="brand" name="implant_brand_{code}" class="id name"></select></div></div></div>');
    add('family_brand_view', '<dl class="dl-horizontal"><dt rel="localize[cases.form.label.{value}_implant]"></dt><dd><span>{family}</span>&nbsp;/&nbsp;<span>{brand}</span></dd></dl>');
    add('bone_parameters_form', '<form data-form-bone="parameters_form_{value}" class="form-horizontal" novalidate="novalidate"></form>');

    add('bone_parameters_nav', '<li><a data-toggle="tab" href="#{id}" rel="localize[cases.form.label.bone_parameters.{value}]"></a></li>');
    add('bone_parameters_nav_panel', '<div class="tab-pane" id="{id}">');

    add('bone_parameters_select', '<div class="control-group"><label class="control-label" rel="localize[cases.form.label.bone_parameters.{code}]"></label><div class="controls"><select name="{code}" class="id name"></select></div></div>');
    add('bone_parameters_option', '<option value="{value}" rel="localize[cases.form.value.bone_parameters.{code}.{value}]"></option>');
    add('bone_parameter_radiogroup', '<div class="control-group"><label class="control-label" rel="localize[cases.form.label.bone_parameters.{code}]"></label><div class="controls" data-param="{code}"/></div>');
    add('bone_parameters_radio', '<label class="radio inline" rel="localize[cases.form.value.bone_parameters.{code}.{value}]"></label><input type="radio" name="{code}" value="{value}">');
    add('bone_parameters_slider_group', '<div class="control-group" date-role="slider" data-type={code}></div>');
    add('bone_parameters_slider', '<label class="control-label" rel="localize[cases.form.label.bone_parameters.{code}]"></label><div class="controls"><i class="icon-minus-sign"></i><input data-id="slider_{code}" type="range" class="carpe-slider" name="{code}"/><i class="icon-plus-sign"></i><input id="{id}_slider_{code}_target" type="text" class="slider-data"/></div>');

    add('case_details_procedure', '<div><div class="clearfix"><h2 class="pull-left" rel="localize[cases.details.procedure_box.title]"></h2></div><div><span>{type}</span></div><div><span>{side}</span></div><div data-role="case_details_box_implant"><strong rel="localize[cases.details.procedure_box.implant]"></strong>:</div>');
    add('case_details_patient', '<div><div class="clearfix"><h2 class="pull-left" rel="localize[cases.details.patient_box.title]"></h2><button class="edit pull-right btn btn-mini btn-success" rel="localize[label.edit]" data-toggle="modal" data-target="#case_details_patient_info_modal"></button></div><div><span>{fullname}</span></div><div><span>{fmt_birthdate}</span></div><div><span>{phone}</span></div><div><span>{email}</span></div></div>');
    add('case_details_surgeon', '<div><div class="clearfix"><h2 class="pull-left" rel="localize[cases.details.surgeon_box.title]"></h2><button class="edit pull-right btn btn-mini btn-success" rel="localize[label.edit]"></button></div><div><a href="#" data-id="{id}">{fullname}</a></div><div><span>{phone}</span></div><div><span><a href="mailto:{email}"><span>{email}</span></a></span></div></div>');
    add('case_details_hospital', '<div><div class="clearfix"><h2 class="pull-left" rel="localize[cases.details.hospital_box.title]"></h2><button class="edit pull-right btn btn-mini btn-success" rel="localize[label.edit]"></button></div><div><span>{name}</span></div><div><span>{street}</span></div><div><span>{city}</span>, <span>{state}</span>, <span>{country}</span></div><div><span>{zip}</span></div></div>');
    add('case_details_procedure_implant', '<div><span data-type="bone">{bone}</span><span class="pull-right" data-type="implant">{family} {brand}</span></div>');

})(jQuery);
