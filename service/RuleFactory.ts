
import Vue from 'vue'

type Rule = (value : any) => true | string

export default class RuleFactory {

  private vueContext : Vue

  constructor (vueContext : Vue) {
    this.vueContext = vueContext
  }
  
  public isRequired () : Rule {
    return value => !!value || this.i18n('validation-rule.field-is-required')
  }

  public maxLength (maxLength : number) : Rule {
    return value => (!value || (<string> value).length <= maxLength ) || this.i18n('validation-rule.max-length', { max: maxLength }) 
  }

  public minLength (minLength : number) : Rule {
    return value => (!value || (<string> value).length >= minLength ) || this.i18n('validation-rule.min-length', { min: minLength }) 
  }

  public max (max : number) : Rule {
    return value => (!value || <number> value <= max ) || this.i18n('validation-rule.max', { max }) 
  }

  public min (min : number) : Rule {
    return value => (!value || <number> value >= min ) || this.i18n('validation-rule.min', { min }) 
  }

  private i18n (i18nCode : string, params ?: any) : string {
    return <string> this.vueContext.$t(i18nCode, params)
  }

}
