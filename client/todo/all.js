(function ($) {
    var uid = 0;

    function generateFormIds() {
        $('.control-group').each(function () {
            var labels = $(this).find('label'),
                inputs = $(this).find(':input');
            if (labels.length == 1 && inputs.length == 1) {
                var id = uid++;
                labels.attr('for', 'uid-' + id);
                inputs.attr('id', 'uid-' + id);
            }
        });
    }

    function checkDuplicateIds() {
        var ids = {};
        $('[id]').each(function () {
            ids[this.id] = ids[this.id] ? ids[this.id] + 1 : 1;
        });
        var total = 0;
        var deleted = 0;
        for (var id in ids) {
            total++;
            if (ids[id] === 1) {
                deleted++;
                delete ids[id];
            }
        }
        if (total != deleted) {
            var msg = 'DUPLICATE IDS FOUND !\n' + $.toJSON(ids);
            if (options.debug) {
                alert(msg);
                throw new Error(msg);
            } else {
                logger.error(msg);
            }
        }
    }

    $.fn.pmpSubmit = function (callback) {
        var form = this;
        this.submit(function () {
            var data = $(this)
                .find('.control-group').removeClass('error')
                .end()
                .find('.help-inline').remove()
                .end()
                .toObject();
            callback.call(form, data);
            return false;
        });
        return this;
    };

    $.fn.showIf = function (bool) {
        return $(this).each(function () {
            if (bool) {
                $(this).show();
            } else {
                $(this).hide();
            }
        });
    };

    $.fn.initForm = function (data) {
        return $(this).each(function () {
            var form = $(this);
            form.find('.control-group').removeClass('error')
                .end()
                .find('.help-inline').remove()
                .end()
                .get(0).reset();
            if (data) {
                var address_check = 0;
                for (var name in data) {
                    var input = form.find(':input[name="' + name + '"]');
                    if (input.length) {
                        if (name === 'state' || name === 'country') {
                            address_check++;
                        }
                        if (input.get(0).tagName.toUpperCase() === 'SELECT') {
                            if ($.isArray(data[name])) {
                                input.templateOptions(data[name]);
                            } else {
                                data[name] = data[name] || '';
                                input.selectOption(data[name]);
                            }
                        } else {
                            input.val(data[name]);
                        }
                    }
                }
                if (address_check === 2 && typeof data.state === 'string') {
                    form.find(':input[name="state"]').selectOption(data.state);
                }
            }
        });
    };

    $.fn.setPatientForm = function (patient, notes, callback) {
        var form = $(this);
        form.find('.control-group').removeClass('error');
        if (patient) {
            form.find('[name=gender][value=' + patient.gender + ']').attr('checked', 'checked');
            form.find('[name=firstname]').val(patient.firstname);
            form.find('[name=middlename]').val(patient.middlename);
            form.find('[name=lastname]').val(patient.lastname);

            var birthdate = moment(patient.birthdate);
            form.find('[name=birth_year]').val(birthdate.year());
            form.find('[name=birth_month]').val(birthdate.month() + 1);
            form.find('[name=birth_day]').val(birthdate.date());

            form.find('[name=phone]').val(patient.phone);
            form.find('[name=email]').val(patient.email);
            form.find('[name=notes]').val(notes);
        }

        form.validate({
            focusInvalid:false,
            rules:{
                gender:{
                    required:true
                },
                firstname:{
                    required:true
                },
                lastname:{
                    required:true
                },
                birth_year:{
                    required:true,
                    number:true,
                    minlength:4,
                    maxlength:4
                },
                birth_month:{
                    required:true,
                    number:true,
                    minlength:1,
                    maxlength:2,
                    range:[1, 12]
                },
                birth_day:{
                    required:true,
                    number:true,
                    minlength:1,
                    maxlength:2,
                    range:[1, 31]
                },
                email:{
                    email:true,
                    required:{
                        depends:function () {
                            return !form.find('[name=phone]').val().length > 0;
                        }
                    }
                },
                phone:{
                    required:{
                        depends:function () {
                            return !form.find('[name=email]').val().length > 0;
                        }
                    }
                }
            },
            messages:{
                gender:'required',
                firstname:'required',
                lastname:'required',
                birth_year:{
                    required:'required',
                    minlength:'',
                    maxlength:'',
                    number:'number'
                },
                birth_month:{
                    required:'required',
                    minlength:'',
                    maxlength:'',
                    number:'number',
                    range:'birth_month_range'
                },
                birth_day:{
                    required:'required',
                    minlength:'',
                    maxlength:'',
                    number:'number',
                    range:'birth_day_range'
                },
                email:{
                    email:'email',
                    required:'required'
                },
                phone:{
                    required:'required'
                }
            },
            groups:{
                date_of_birth:'birth_year birth_month birth_day',
                name:'firstname lastname',
                contact:'phone email'
            },
            invalidHandler:function (form, validator) {
                $(this).find('.control-group').removeClass('error').find('.help-inline').remove();
                var _errors = {};
                var index = 0;

                for (var field in validator.invalid) {
                    if (validator.groups[field]) {
                        if (field == 'firstname' || field == 'lastname') {
                            _errors['name'] = 'required';
                        }
                        if (field == 'birth_year' || field == 'birth_month' || field == 'birth_day') {
                            _errors['date_of_birth'] = validator.invalid[field];
                        }
                        if (field == 'phone' || field == 'email') {
                            if (validator.invalid[field] == 'required') {
                                _errors['contact'] = validator.invalid[field];
                            } else {
                                _errors['email'] = validator.invalid[field];
                            }
                        }
                    } else {
                        _errors[field] = validator.errorList[index].message;
                    }
                    index++;
                }
                $(this).templateErrors('cases.form.error', _errors);
            },
            errorPlacement:function () {
            },
            submitHandler:function () {
                var form = $(this.currentForm);
                form.find('.control-group').removeClass('error').find('.help-inline').remove();
                var _errors = {};
                var year = form.find('[name=birth_year]').val();
                var month = form.find('[name=birth_month]').val();
                var day = form.find('[name=birth_day]').val();

                if (new Date(year, month - 1, day).getDate() == day) {
                    callback(form);
                }
                else {
                    _errors['date_of_birth'] = 'invalid';
                    form.templateErrors('cases.form.error', _errors);
                }
            }
        });
    };

    //  Data format
    //  - procedure_type: the procedure type
    //  - parameters: surgical parameters
    //  - is_editable: view or edit
    $.fn.populateSurgicalParameters = function (data) {
        var root = $(this);
        var bone_nav = root.find('ul').empty();
        var bone_params = root.find('.modal-body').empty();

        root.find('h3 span:first').text($.grep(zimmer.spec.get('procedure_type'), function (element) {
            return element.code == data.procedure_type;
        })[0].name);

        var bones_code = $.grep(zimmer.spec.get('procedure_type'), function (element) {
            return element.code == data.procedure_type;
        })[0].bones;

        if (bones_code) {
            var bones = $.grep(zimmer.spec.get('bones'), function (element) {
                return bones_code.indexOf(element.code) != -1;
            });

            $.each(bones, function (index, bone) {
                // Add bone Navbar element and panel
                var id = uuid.v1();
                bone_nav.append(zimmer.template['bone_parameters_nav'].merge({id:id, value:bone.value}));
                bone_params.append(zimmer.template['bone_parameters_nav_panel'].merge({id:id}));
                bone_nav.find('li:first').addClass('active');
                bone_params.find('div:first').addClass('active');
                i18n.localize(bone_nav);

                var parameters;
                if (data.parameters) {
                    parameters = data.parameters[bone.code].parameters;
                }

                var form = $('#' + id).append(zimmer.template['bone_parameters_form'].merge(bone)).find('form:first');
                form.attr('id', uuid.v1());
                $.each(bone.parameters, function (index, code_param) {
                    var bone_param = $.grep(zimmer.spec.get('parameters'), function (param) {
                        return param.code == code_param;
                    });

                    var bones = $.grep(zimmer.spec.get('bones'), function (element) {
                        return bones_code.indexOf(element.code) != -1;
                    });

                    $.each(bone_param, function (index, param) {
                        switch (param.type) {
                            case 'choice':
                                form.append(zimmer.template['bone_parameters_select'].merge(param));
                                // TODO hack => business rule hard coded
                                form.find('select[name=' + param.code + ']').change(function () {
                                    var slider_tag;
                                    switch ($(this).find('option:selected').val()) {
                                        case 'TEA':
                                            slider_tag = form.find('[date-role=slider][data-type=852]').empty();
                                            slider_tag.append(zimmer.template['bone_parameters_slider'].merge({id:id, code:'852'}));
                                            CARPE.Sliders.make(form.find('[data-id=slider_852]').get(0), {
                                                'orientation':'horizontal',
                                                'targets':id + '_slider_852_target',
                                                'size':175,
                                                'value':0,
                                                'decimals':1,
                                                'zerofill':'yes',
                                                'min':-3,
                                                'max':0,
                                                'step':0.5,
                                                'disabled':data.is_editable
                                            });
                                            i18n.localize(slider_tag);
                                            break;
                                        case 'PCA':
                                            slider_tag = form.find('[date-role=slider][data-type=852]').empty();
                                            slider_tag.append(zimmer.template['bone_parameters_slider'].merge({id:id, code:'852'}));
                                            CARPE.Sliders.make(form.find('[data-id=slider_852]').get(0), {
                                                'orientation':'horizontal',
                                                'targets':id + '_slider_852_target',
                                                'size':175,
                                                'value':3,
                                                'decimals':1,
                                                'zerofill':'yes',
                                                'min':0,
                                                'max':9,
                                                'step':0.5,
                                                'disabled':data.is_editable
                                            });
                                            i18n.localize(slider_tag);
                                            break;
                                        case 'WHITESIDE':
                                            slider_tag = form.find('[date-role=slider][data-type=852]').empty();
                                            slider_tag.append(zimmer.template['bone_parameters_slider'].merge({id:id, code:'852'}));
                                            CARPE.Sliders.make(form.find('[data-id=slider_852]').get(0), {
                                                'orientation':'horizontal',
                                                'targets':id + '_slider_852_target',
                                                'size':175,
                                                'value':0,
                                                'decimals':1,
                                                'zerofill':'yes',
                                                'min':0,
                                                'max':6,
                                                'step':0.5,
                                                'disabled':data.is_editable
                                            });
                                            i18n.localize(slider_tag);
                                            break;
                                    }
                                });
                                // TODO end hack

                                $.each(param.values, function (index, element) {
                                    form.find('select[name=' + param.code + ']').append(zimmer.template['bone_parameters_option'].merge({value:element, code:param.code}));
                                });
                                form.find('select[name=' + param.code + ']').find('option[value="' + (parameters ? parameters[param.code] : param['default']) + '"]').attr('selected', 'selected');

                                break;
                            case 'yesno':
                                form.append(zimmer.template['bone_parameter_radiogroup'].merge(param));
                                $.each(param.values, function (index, element) {
                                    form.find('[data-param=' + param.code + ']').append(zimmer.template['bone_parameters_radio'].merge({value:element, code:param.code}));
                                });
                                form.find('[data-param=' + param.code + ']').find('[value="' + (parameters ? parameters[param.code] : param['default']) + '"]').attr('checked', true);
                                break;
                            case 'range':
                                var slider_tag = zimmer.template['bone_parameters_slider_group'].merge(param);
                                slider_tag.append(zimmer.template['bone_parameters_slider'].merge({id:id, code:param.code}));
                                form.append(slider_tag);

                                var slider = CARPE.Sliders.make(form.find('[data-id=slider_' + param.code + ']').get(0), {
                                    'orientation':'horizontal',
                                    'targets':id + '_slider_' + param.code + '_target',
                                    'size':175,
                                    'value':parameters ? parseInt(parameters[param.code]) : param['default'],
                                    'decimals':1,
                                    'zerofill':'yes',
                                    'min':param.min,
                                    'max':param.max,
                                    'step':param.step,
                                    'disabled':data.is_editable
                                });
                                if (data.is_editable) {
                                    form.find('[data-id=slider_' + param.code + '_target]').click(function () {
                                        $(this).val('');
                                    }).keyup(function () {
                                            slider.setValue($(this).val());
                                        });
                                    form.find('[data-type=' + param.code + ']').find('i:first').click(function () {
                                        slider.setValue(Number(slider.value) - slider.step);
                                    })
                                        .end()
                                        .find('i:last').click(function () {
                                            slider.setValue(Number(slider.value) + slider.step);
                                        });
                                }
                                break;
                        }
                    });
                });

                if (!data.is_editable) {
                    form.find('input, select').attr('disabled', 'disabled');
                }

                i18n.localize(form);
            });
        }
    };

    bus.topic('domready').subscribe(function () {
        generateFormIds();
        checkDuplicateIds();
    });

})(jQuery);
