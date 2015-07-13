package simpledms

import grails.transaction.Transactional

class DocumentController {

	static allowedMethods = [save: "POST"]

	def index() {
		redirect(action: "list", params: params)
	}

	def list() {
		params.max = 10
		[documentInstanceList: Document.list(params), documentInstanceTotal: Document.count()]
	}

	def create() {
	}
	
	def show(Document documentInstance) {
		respond documentInstance
	}
	
	@Transactional
    def save(Document documentInstance) {
        if (documentInstance == null) {
            notFound()
            return
        }

        if (documentInstance.hasErrors()) {
            respond documentInstance.errors, view:'create'
            return
        }

        documentInstance.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'document.label', default: 'Document'), documentInstance.id])
                redirect documentInstance
            }
            '*' { respond documentInstance, [status: CREATED] }
        }
    }

    def edit(Document documentInstance) {
        respond documentInstance
    }
	def upload() {
		def file = request.getFile('file')
		if(file.empty) {
			flash.message = "File cannot be empty"
		} else {
			def documentInstance = new Document()
			documentInstance.filename = file.originalFilename
			documentInstance.filedata = file.getBytes()
			documentInstance.save()
		}
		redirect (action:'list')
	}

 // @Transactional
    def update(Document documentInstance) {
        def file = request.getFile('file')
		if(file.empty) {
			flash.message = "File cannot be empty"
		} else {
		 	documentInstance.save flush:true
			documentInstance.filename = file.originalFilename
			documentInstance.filedata = file.getBytes()
			documentInstance.save()
		}
		redirect (action:'list')
	}


    @Transactional
    def delete(Document documentInstance) {

        if (documentInstance == null) {
            notFound()
            return
        }

        documentInstance.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'Document.label', default: 'Document'), documentInstance.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'document.label', default: 'Document'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
	

	def download(long id) {
		Document documentInstance = Document.get(id)
		if ( documentInstance == null) {
			flash.message = "Document not found."
			redirect (action:'list')
		} else {
			response.setContentType("APPLICATION/OCTET-STREAM")
			response.setHeader("Content-Disposition", "Attachment;Filename=\"${documentInstance.filename}\"")
			def outputStream = response.getOutputStream()
			outputStream << documentInstance.filedata
			outputStream.flush()
			outputStream.close()
		}
	}
}

